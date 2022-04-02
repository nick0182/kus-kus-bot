package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Ingredient;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.ReceiptKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.append;

/**
 * Shows receipt ingredients
 */
@Slf4j
public record ReceiptIngredientsPageBotProcessor(StringCacheService cacheService, ReceiptService receiptService,
                                                 ReceiptKeyboardProvider keyboardProvider) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageCaption> process(Data data) {
        int receiptId = data.getSession().getReceiptId();
        String userId = data.getUserId();
        return getReceiptFromCache(userId)
                .filter(receiptValue -> receiptValue.queryParam() == receiptId)
                .switchIfEmpty(updateReceiptInCache(receiptId, userId))
                .zipWith(keyboardProvider
                        .compileKeyboard(data, Data.Action.SHOW_RECEIPT_NUTRITION_PAGE, "Энергетическая ценность"))
                .map(tuple2 -> EditMessageCaption.builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .caption(createIngredientsCaption(tuple2.getT1().ingredients()))
                        .replyMarkup(tuple2.getT2())
                        .build());
    }

    private Mono<ReceiptValue> getReceiptFromCache(String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Getting receipt from cache");
        }
        return cacheService.getReceipt(userId);
    }

    private Mono<ReceiptValue> updateReceiptInCache(int receiptId, String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "No receipt found in cache. Updating cache with receipt id: {}", receiptId);
        }
        return receiptService
                .getReceipt(receiptId)
                .filterWhen(receipt -> cacheService.storeReceipt(userId, receipt));
    }

    private String createIngredientsCaption(List<Ingredient> ingredients) {
        return ingredients.stream().map(ingredient -> {
            String quantity = ingredient.quantity() != null ? String.valueOf(ingredient.quantity()) : "";
            String measurement = ingredient.measurement() != null ? ingredient.measurement() : "";
            return ingredient.name() + " - " + quantity + " " + measurement + "\n";
        }).collect(Collectors.joining());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.RECEIPT_WITH_INGREDIENTS_PAGE;
    }
}
