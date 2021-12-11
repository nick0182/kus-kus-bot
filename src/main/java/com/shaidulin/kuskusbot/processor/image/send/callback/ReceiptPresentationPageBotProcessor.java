package com.shaidulin.kuskusbot.processor.image.send.callback;

import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.send.ImageSendBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import com.shaidulin.kuskusbot.util.SortType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Shows the first page of receipt presentation
 */
@Slf4j
public record ReceiptPresentationPageBotProcessor(StringCacheService stringCacheService,
                                                  ReceiptService receiptService,
                                                  ImageService imageService,
                                                  int receiptPageSize) implements ImageSendBotProcessor {

    @Override
    public Mono<? extends SendPhoto> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        SortType sortType = SortType.valueOf(callbackWrapper.data());
        return stringCacheService
                .getIngredients(callbackWrapper.userId())
                .flatMap(ingredients -> receiptService
                        .getReceiptPresentations(ingredients, new Page(0, receiptPageSize), sortType))
                .filterWhen(receiptPresentationMatch -> stringCacheService
                        .storeReceiptPresentations(callbackWrapper.userId(), receiptPresentationMatch))
                .map(ReceiptPresentationMatch::receipts)
                .flatMap(receiptPresentations -> provideMessage(receiptPresentations.get(0), callbackWrapper.chatId(),
                        receiptPresentations.size() > 1));
    }

    private Mono<SendPhoto> provideMessage(ReceiptPresentationValue receiptPresentation, String chatId, boolean hasMore) {
        InlineKeyboardMarkup keyboard =
                KeyboardCreator.createReceiptPresentationKeyboard(0, receiptPresentation.queryParam(), hasMore);

        log.debug("Got receipt presentation to render: {}", receiptPresentation);

        String imageId = String.valueOf(receiptPresentation.queryParam());

        return stringCacheService
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

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGE;
    }
}
