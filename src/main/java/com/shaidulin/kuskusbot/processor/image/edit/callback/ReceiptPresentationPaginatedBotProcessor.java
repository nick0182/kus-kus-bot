package com.shaidulin.kuskusbot.processor.image.edit.callback;

import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * Shows a page of receipt presentation
 */
@Slf4j
public record ReceiptPresentationPaginatedBotProcessor(StringCacheService stringCacheService,
                                                       ReceiptService receiptService,
                                                       ImageService imageService,
                                                       int receiptPageSize) implements ImageEditBotProcessor {

    @Override
    public Mono<? extends EditMessageMedia> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        String userId = callbackWrapper.userId();
        int page = Integer.parseInt(callbackWrapper.data());
        return stringCacheService
                .getReceiptPresentationsMeta(userId)
                .flatMap(meta -> {
                    int cacheIndex = page % receiptPageSize;
                    int currentBatch = page / receiptPageSize;
                    if (currentBatch < meta.batch()) { // get previous batch
                        return getReceiptsAndStoreInCache(userId, new Page(page + 1 - receiptPageSize, receiptPageSize), meta.sortType())
                                .flatMap(receiptPresentations ->
                                        provideMessage(receiptPresentations.get(receiptPresentations.size() - 1),
                                                callbackWrapper, page, true));
                    } else if (currentBatch > meta.batch()) { // get next batch
                        return getReceiptsAndStoreInCache(userId, new Page(page, receiptPageSize), meta.sortType())
                                .flatMap(receiptPresentations -> provideMessage(receiptPresentations.get(0),
                                        callbackWrapper, page, receiptPresentations.size() > 1));
                    } else { // get from current cache
                        if (cacheIndex == 0) {
                            return getFromCache(callbackWrapper, cacheIndex, page, true);
                        } else if (cacheIndex == receiptPageSize - 1) {
                            return getFromCache(callbackWrapper, cacheIndex, page, meta.hasMore());
                        } else {
                            return stringCacheService
                                    .getReceiptPresentationsSize(userId)
                                    .flatMap(cacheSize -> getFromCache(callbackWrapper, cacheIndex, page,
                                            cacheSize != cacheIndex + 1 || meta.hasMore()));
                        }
                    }
                });
    }

    private Mono<EditMessageMedia> getFromCache(CallbackMapper.Wrapper callbackWrapper,
                                                int cacheIndex, int page, boolean hasMore) {
        return stringCacheService
                .getReceiptPresentation(callbackWrapper.userId(), cacheIndex)
                .flatMap(receiptPresentation -> provideMessage(receiptPresentation, callbackWrapper, page, hasMore));
    }

    private Mono<List<ReceiptPresentationValue>> getReceiptsAndStoreInCache(String userId, Page page, SortType sortType) {
        return stringCacheService
                .getIngredients(userId)
                .flatMap(ingredients -> receiptService
                        .getReceiptPresentations(ingredients, page, sortType))
                .filterWhen(receiptPresentationMatch -> stringCacheService
                        .storeReceiptPresentations(userId, receiptPresentationMatch))
                .map(ReceiptPresentationMatch::receipts);
    }

    private Mono<EditMessageMedia> provideMessage(ReceiptPresentationValue receiptPresentation,
                                                  CallbackMapper.Wrapper callbackWrapper, int page, boolean hasMore) {
        InlineKeyboardMarkup keyboard =
                KeyboardCreator.createReceiptPresentationKeyboard(page, receiptPresentation.queryParam(), hasMore);

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
