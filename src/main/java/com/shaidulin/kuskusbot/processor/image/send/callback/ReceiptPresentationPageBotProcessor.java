package com.shaidulin.kuskusbot.processor.image.send.callback;

import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.send.ImageSendBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.ReceiptPageKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ImageType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Shows the first page of receipt presentation
 */
@Slf4j
public record ReceiptPresentationPageBotProcessor(StringCacheService cacheService,
                                                  ReceiptService receiptService,
                                                  ReceiptPageKeyboardProvider keyboardProvider,
                                                  ImageService imageService,
                                                  int receiptPageSize) implements ImageSendBotProcessor {

    @Override
    public Mono<SendPhoto> process(Data data) {
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
                .zipWhen(receipts -> keyboardProvider.compileKeyboard(data, receipts.get(0).queryParam(), receipts.size() > 1))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1().get(0), data.getChatId(), tuple2.getT2()));
    }

    private Mono<SendPhoto> provideMessage(ReceiptPresentationValue receiptPresentation,
                                           String chatId,
                                           InlineKeyboardMarkup keyboard) {
        log.debug("Got receipt presentation to render: {}", receiptPresentation);

        String imageId = String.valueOf(receiptPresentation.queryParam());

        return cacheService
                .getImage(imageId)
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

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGE;
    }
}
