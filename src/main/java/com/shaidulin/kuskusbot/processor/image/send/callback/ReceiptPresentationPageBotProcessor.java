package com.shaidulin.kuskusbot.processor.image.send.callback;

import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.send.ImageSendBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Objects;
import java.util.UUID;

/**
 * Shows the first page of receipt presentation
 */
@Slf4j
public record ReceiptPresentationPageBotProcessor(StringCacheService cacheService,
                                                  ReceiptService receiptService,
                                                  ImageService imageService,
                                                  int receiptPageSize) implements ImageSendBotProcessor {

    @Override
    public Mono<? extends SendPhoto> process(Data data) {
        return cacheService
                .getIngredients(data.getUserId())
                .flatMap(ingredients -> receiptService
                        .getReceiptPresentations(
                                ingredients,
                                new Page(0, receiptPageSize),
                                data.getSession().getReceiptSortType()))
                .filterWhen(receiptPresentationMatch -> cacheService
                        .storeReceiptPresentations(data.getUserId(), receiptPresentationMatch))
                .map(ReceiptPresentationMatch::receipts)
                .zipWhen(receiptPresentations -> compileNextPageButton(data, receiptPresentations.size() > 1))
                .zipWhen(tuple2 -> compileReceiptIngredientsButton(data, tuple2.getT1().get(0).queryParam()),
                        (tuple2, receiptIngredientsButtonKey) ->
                                Tuples.of(tuple2.getT1().get(0),
                                        DynamicKeyboard.createReceiptPresentationKeyboard(
                                                receiptIngredientsButtonKey,
                                                DynamicKeyboard.NULL_KEY_UUID,
                                                tuple2.getT2())))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1(), data.getChatId(), tuple2.getT2()));
    }

    private Mono<SendPhoto> provideMessage(ReceiptPresentationValue receiptPresentation,
                                           String chatId,
                                           InlineKeyboardMarkup keyboard) {
        log.debug("Got receipt presentation to render: {}", receiptPresentation);

        String imageId = String.valueOf(receiptPresentation.queryParam());

        return cacheService
                .getImage(imageId, ImageType.MAIN)
                .map(cachedImage -> compileMessage(cachedImage, null, chatId, receiptPresentation, keyboard))
                .switchIfEmpty(imageService
                        .fetchImage(ImageType.MAIN, imageId)
                        .map(imageBytes -> compileMessage(null, imageBytes, chatId, receiptPresentation, keyboard))
                );
    }

    @SneakyThrows
    private SendPhoto compileMessage(String cachedImage, byte[] imageBytes, String chatId,
                                     ReceiptPresentationValue receiptPresentation, InlineKeyboardMarkup keyboard) {
        boolean isNewMedia = Objects.isNull(cachedImage);
        String imageName = String.valueOf(receiptPresentation.queryParam());

        InputFile photo = new InputFile();
        if (isNewMedia) {
            log.debug("Compiling photo from resource with length: {} and name: {}", imageBytes.length, imageName);
            photo.setMedia(new ByteArrayResource(imageBytes).getInputStream(), imageName);
        } else {
            log.debug("Compiling photo from cache with name: {}", imageName);
            photo.setMedia(cachedImage);
        }

        return SendPhoto.builder()
                .chatId(chatId)
                .photo(photo)
                .caption(imageService.createPhotoCaption(receiptPresentation))
                .replyMarkup(keyboard)
                .build();
    }

    private Mono<UUID> compileReceiptIngredientsButton(Data data, int receiptId) {
        Data.Session currentSession = data.getSession();
        UUID key = UUID.randomUUID();
        Data.Session session = Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_INGREDIENTS_PAGE)
                .receiptSortType(currentSession.getReceiptSortType())
                .receiptId(receiptId)
                .currentReceiptPage(0)
                .build();
        return cacheService
                .storeSession(data.getUserId(), key, session)
                .map(ignored -> key);
    }

    private Mono<UUID> compileNextPageButton(Data data, boolean hasMoreReceipts) {
        if (hasMoreReceipts) {
            Data.Session currentSession = data.getSession();
            UUID key = UUID.randomUUID();
            Data.Session session = Data.Session
                    .builder()
                    .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                    .receiptSortType(currentSession.getReceiptSortType())
                    .currentReceiptPage(1)
                    .build();
            return cacheService
                    .storeSession(data.getUserId(), key, session)
                    .map(ignored -> key);
        } else {
            return Mono.just(DynamicKeyboard.NULL_KEY_UUID);
        }
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGE;
    }
}
