package com.shaidulin.kuskusbot.processor.image.edit.callback;

import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Shows a paginated receipt presentation
 */
@Slf4j
public record ReceiptPresentationPaginatedBotProcessor(StringCacheService cacheService,
                                                       ReceiptService receiptService,
                                                       ImageService imageService,
                                                       int receiptPageSize) implements ImageEditBotProcessor {

    @Override
    public Mono<? extends EditMessageMedia> process(Data data) {
        String userId = data.getUserId();
        int page = data.getSession().getCurrentReceiptPage();
        return cacheService
                .getReceiptPresentationsMeta(userId)
                .flatMap(meta -> {
                    int cacheIndex = page % receiptPageSize;
                    int currentBatch = page / receiptPageSize;
                    if (currentBatch < meta.batch()) { // get previous batch
                        return getReceiptsAndStoreInCache(userId, new Page(page + 1 - receiptPageSize, receiptPageSize), meta.sortType())
                                .map(receiptPresentations -> receiptPresentations.get(receiptPresentations.size() - 1))
                                .zipWhen(receiptPresentation -> compileReceiptIngredientsButton(data, receiptPresentation.queryParam(), true))
                                .flatMap(tuple2 -> provideMessage(tuple2.getT1(), data, tuple2.getT2()));
                    } else if (currentBatch > meta.batch()) { // get next batch
                        return getReceiptsAndStoreInCache(userId, new Page(page, receiptPageSize), meta.sortType())
                                .zipWhen(receiptPresentations -> compileReceiptIngredientsButton(data, receiptPresentations.get(0).queryParam(), receiptPresentations.size() > 1))
                                .flatMap(tuple2 -> provideMessage(tuple2.getT1().get(0), data, tuple2.getT2()));
                    } else { // get from current cache
                        if (cacheIndex == 0) {
                            return getFromCache(data, cacheIndex, true);
                        } else if (cacheIndex == receiptPageSize - 1) {
                            return getFromCache(data, cacheIndex, meta.hasMore());
                        } else {
                            return cacheService
                                    .getReceiptPresentationsSize(userId)
                                    .flatMap(cacheSize -> getFromCache(data, cacheIndex,
                                            cacheSize != cacheIndex + 1 || meta.hasMore()));
                        }
                    }
                });
    }

    private Mono<EditMessageMedia> getFromCache(Data data, int cacheIndex, boolean hasMoreReceipts) {
        return cacheService
                .getReceiptPresentation(data.getUserId(), cacheIndex)
                .zipWhen(receiptPresentation -> compileReceiptIngredientsButton(data, receiptPresentation.queryParam(), hasMoreReceipts))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1(), data, tuple2.getT2()));
    }

    private Mono<List<ReceiptPresentationValue>> getReceiptsAndStoreInCache(String userId, Page page, SortType sortType) {
        return cacheService
                .getIngredients(userId)
                .flatMap(ingredients -> receiptService
                        .getReceiptPresentations(ingredients, page, sortType))
                .filterWhen(receiptPresentationMatch -> cacheService
                        .storeReceiptPresentations(userId, receiptPresentationMatch))
                .map(ReceiptPresentationMatch::receipts);
    }

    private Mono<EditMessageMedia> provideMessage(ReceiptPresentationValue receiptPresentation,
                                                  Data data,
                                                  InlineKeyboardMarkup keyboard) {
        log.debug("Got receipt presentation to render: {}", receiptPresentation);

        String imageId = String.valueOf(receiptPresentation.queryParam());

        return cacheService
                .getImage(imageId, ImageType.MAIN)
                .map(cachedImage -> compileMessage(cachedImage, null, data, receiptPresentation, keyboard))
                .switchIfEmpty(imageService
                        .fetchImage(ImageType.MAIN, imageId)
                        .map(imageBytes -> compileMessage(null, imageBytes, data, receiptPresentation, keyboard))
                );
    }

    @SneakyThrows
    private EditMessageMedia compileMessage(String cachedImage, byte[] imageBytes, Data data,
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
                .chatId(data.getChatId())
                .messageId(data.getMessageId())
                .media(photo)
                .replyMarkup(keyboard)
                .build();
    }

    private Mono<InlineKeyboardMarkup> compileReceiptIngredientsButton(Data data, int receiptId, boolean hasMoreReceipts) {
        Data.Session currentSession = data.getSession();
        int currentReceiptPage = currentSession.getCurrentReceiptPage();
        SortType receiptSortType = currentSession.getReceiptSortType();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonCurrentIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Ингредиенты", String.valueOf(buttonCurrentIndex)));
        sessionHash.put(buttonCurrentIndex, Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_INGREDIENTS_PAGE)
                .receiptSortType(receiptSortType)
                .receiptId(receiptId)
                .currentReceiptPage(currentReceiptPage)
                .build());

        Integer previousPageButtonIndex = null;
        Integer nextPageButtonIndex = null;

        if (currentReceiptPage > 0) {
            previousPageButtonIndex = ++buttonCurrentIndex;
            sessionHash.put(previousPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                    .receiptSortType(receiptSortType)
                    .currentReceiptPage(currentReceiptPage - 1)
                    .build());
        }

        if (hasMoreReceipts) {
            nextPageButtonIndex = ++buttonCurrentIndex;
            sessionHash.put(nextPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                    .receiptSortType(receiptSortType)
                    .currentReceiptPage(currentReceiptPage + 1)
                    .build());
        }

        buttons.add(DynamicKeyboard.createNavigationPanelRow(previousPageButtonIndex, nextPageButtonIndex));

        return cacheService
                .storeSession(data.getUserId(), sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGINATED;
    }
}
