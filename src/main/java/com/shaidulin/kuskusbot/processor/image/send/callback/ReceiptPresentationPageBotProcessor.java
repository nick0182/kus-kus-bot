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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

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
                .zipWhen(receipts -> compileReceiptIngredientsButton(data, receipts.get(0).queryParam(), receipts.size() > 1))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1().get(0), data.getChatId(), tuple2.getT2()));
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

    private Mono<InlineKeyboardMarkup> compileReceiptIngredientsButton(Data data, int receiptId, boolean hasMoreReceipts) {
        Data.Session currentSession = data.getSession();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonCurrentIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Ингредиенты", String.valueOf(buttonCurrentIndex)));
        sessionHash.put(buttonCurrentIndex, Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_INGREDIENTS_PAGE)
                .receiptSortType(currentSession.getReceiptSortType())
                .receiptId(receiptId)
                .currentReceiptPage(0)
                .build());

        if (hasMoreReceipts) {
            buttonCurrentIndex++;
            buttons.add(DynamicKeyboard.createNavigationPanelRow(null, buttonCurrentIndex));
            sessionHash.put(buttonCurrentIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                    .receiptSortType(currentSession.getReceiptSortType())
                    .currentReceiptPage(1)
                    .build());
        }

        return cacheService
                .storeSession(data.getUserId(), sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGE;
    }
}
