package com.shaidulin.kuskusbot.processor.image.edit.callback;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.cache.ReceiptPresentationCacheService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j
public record ReceiptPresentationPaginatedBotProcessor(StringCacheService stringCacheService,
                                                       ReceiptPresentationCacheService receiptPresentationCacheService,
                                                       ImageService imageService) implements ImageEditBotProcessor {

    @Override
    public Mono<? extends EditMessageMedia> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        int page = Integer.parseInt(callbackWrapper.data());
        return receiptPresentationCacheService
                .getReceiptPresentations(callbackWrapper.userId())
                .flatMap(receiptPresentations -> provideMessage(receiptPresentations, callbackWrapper, page));
    }

    private Mono<EditMessageMedia> provideMessage(List<ReceiptPresentationValue> receiptPresentations,
                                                  CallbackMapper.Wrapper callbackWrapper, int page) {
        InlineKeyboardMarkup keyboard = KeyboardCreator.createReceiptKeyboard(receiptPresentations, page);

        ReceiptPresentationValue receiptPresentation = receiptPresentations.get(page);

        log.debug("Got receipt presentation to render: {}", receiptPresentation);

        String imageId = String.valueOf(receiptPresentation.queryParam());

        return stringCacheService
                .getImage(imageId, ImageType.MAIN)
                .map(cachedImage -> compileMessage(cachedImage, null, callbackWrapper, receiptPresentation, keyboard))
                .switchIfEmpty(imageService
                        .fetchImage(ImageType.MAIN, imageId)
                        .map(imageBytes -> compileMessage(null, imageBytes, callbackWrapper, receiptPresentation, keyboard))
                );
    }

    @SneakyThrows
    private EditMessageMedia compileMessage(String cachedImage, byte[] imageBytes, CallbackMapper.Wrapper callbackWrapper,
                                     ReceiptPresentationValue receiptPresentation, InlineKeyboardMarkup keyboard) {
        boolean isNewMedia = Objects.isNull(cachedImage);
        String imageName = String.valueOf(receiptPresentation.queryParam());

        InputMedia photo = new InputMediaPhoto();
        photo.setCaption(imageService.createPhotoCaption(receiptPresentation));
        if (isNewMedia) {
            log.debug("Compiling photo from resource with length: {} and name: {}", imageBytes.length, imageName);
            photo.setMedia(new ByteArrayResource(imageBytes).getInputStream(), imageName);
        } else {
            log.debug("Compiling photo from cache with name: {}", imageName);
            photo.setMedia(cachedImage);
        }

        return EditMessageMedia.builder()
                .chatId(callbackWrapper.chatId())
                .messageId(callbackWrapper.messageId())
                .media(photo)
                .replyMarkup(keyboard)
                .build();
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGINATED;
    }
}
