package com.shaidulin.kuskusbot.processor.image.edit.callback;

import com.shaidulin.kuskusbot.dto.receipt.Meta;
import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.processor.image.edit.ImageEditBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.MediaMessageProvider;
import com.shaidulin.kuskusbot.service.util.ReceiptPageKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.SortType;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

import java.util.List;

import static net.logstash.logback.marker.Markers.append;

/**
 * Shows a paginated receipt presentation
 */
@Slf4j
public record ReceiptPresentationPaginatedBotProcessor(StringCacheService cacheService,
                                                       ReceiptService receiptService,
                                                       ReceiptPageKeyboardProvider keyboardProvider,
                                                       MediaMessageProvider<EditMessageMedia> editMessageMediaProvider,
                                                       int receiptPageSize) implements ImageEditBotProcessor {

    @Override
    public Mono<EditMessageMedia> process(Data data) {
        String userId = data.getUserId();
        int page = data.getSession().getCurrentReceiptPage();
        return getReceiptPresentationsMetaFromCache(userId)
                .flatMap(meta -> {
                    int currentBatch = page / receiptPageSize;
                    SortType sortType = meta.sortType();
                    if (currentBatch < meta.batch()) {
                        return previousBatchFlow(data, sortType);
                    } else if (currentBatch > meta.batch()) {
                        return nextBatchFlow(data, sortType);
                    } else {
                        return currentBatchFlow(data, meta.hasMore());
                    }
                });
    }

    private Mono<Meta> getReceiptPresentationsMetaFromCache(String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Getting receipt presentations meta from cache");
        }
        return cacheService.getReceiptPresentationsMeta(userId);
    }

    private Mono<EditMessageMedia> previousBatchFlow(Data data, SortType sortType) {
        String userId = data.getUserId();
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Previous batch flow");
        }
        Page page = new Page(data.getSession().getCurrentReceiptPage() + 1 - receiptPageSize, receiptPageSize);
        return getReceiptsAndStoreInCache(userId, page, sortType)
                .map(receiptPresentations -> receiptPresentations.get(receiptPresentations.size() - 1))
                .zipWhen(receiptPresentation -> keyboardProvider.compileKeyboard(data, receiptPresentation.queryParam(), true))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1(), data, tuple2.getT2()));
    }

    private Mono<EditMessageMedia> nextBatchFlow(Data data, SortType sortType) {
        String userId = data.getUserId();
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Next batch flow");
        }
        Page page = new Page(data.getSession().getCurrentReceiptPage(), receiptPageSize);
        return getReceiptsAndStoreInCache(userId, page, sortType)
                .zipWhen(receiptPresentations -> keyboardProvider.compileKeyboard(
                        data, receiptPresentations.get(0).queryParam(), receiptPresentations.size() > 1))
                .flatMap(tuple2 -> provideMessage(tuple2.getT1().get(0), data, tuple2.getT2()));
    }

    private Mono<EditMessageMedia> currentBatchFlow(Data data, boolean hasMore) {
        String userId = data.getUserId();
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Current batch flow");
        }
        int cacheIndex = data.getSession().getCurrentReceiptPage() % receiptPageSize;
        if (cacheIndex == 0) {
            return getFromCache(data, cacheIndex, true);
        } else if (cacheIndex == receiptPageSize - 1) {
            return getFromCache(data, cacheIndex, hasMore);
        } else {
            return cacheService
                    .getReceiptPresentationsSize(userId)
                    .flatMap(cacheSize -> getFromCache(data, cacheIndex, cacheSize != cacheIndex + 1 || hasMore));
        }
    }

    private Mono<EditMessageMedia> getFromCache(Data data, int cacheIndex, boolean hasMoreReceipts) {
        return cacheService
                .getReceiptPresentation(data.getUserId(), cacheIndex)
                .zipWhen(receiptPresentation -> keyboardProvider.compileKeyboard(data, receiptPresentation.queryParam(), hasMoreReceipts))
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
        String name = String.valueOf(receiptPresentation.queryParam());
        String caption = MediaMessageProvider.createPhotoCaption(receiptPresentation);
        return editMessageMediaProvider.provideMessage(name, caption, ImageType.MAIN, data, keyboard);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_PRESENTATION_PAGINATED;
    }
}
