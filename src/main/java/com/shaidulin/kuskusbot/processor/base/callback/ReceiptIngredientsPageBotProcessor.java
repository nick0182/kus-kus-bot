package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Ingredient;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.ButtonConstants;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Shows receipt ingredients
 */
public record ReceiptIngredientsPageBotProcessor(StringCacheService cacheService,
                                                 ReceiptService receiptService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageCaption> process(Data data) {
        int receiptId = data.getSession().getReceiptId();
        return cacheService
                .getReceipt(data.getUserId())
                .filter(receiptValue -> receiptValue.queryParam() == receiptId)
                .switchIfEmpty(receiptService
                        .getReceipt(receiptId)
                        .filterWhen(receipt -> cacheService.storeReceipt(data.getUserId(), receipt)))
                .zipWith(compileButton(data, Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE))
                .zipWith(compileButton(data, Data.Action.SHOW_RECEIPT_NUTRITION_PAGE))
                .zipWith(compileButton(data, Data.Action.SHOW_STEP_PAGE),
                        (tuple2OfTuple2, stepButtonKey) -> Tuples.of(
                                tuple2OfTuple2.getT1().getT1().ingredients(),
                                DynamicKeyboard.createReceiptKeyboard(
                                        tuple2OfTuple2.getT1().getT2(),
                                        Map.of(ButtonConstants.SHOW_NUTRITION_OVERVIEW, tuple2OfTuple2.getT2(),
                                                ButtonConstants.SHOW_STEPS, stepButtonKey))))
                .map(tuple2 -> EditMessageCaption.builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .caption(createIngredientsCaption(tuple2.getT1()))
                        .replyMarkup(tuple2.getT2())
                        .build());
    }

    private Mono<UUID> compileButton(Data data, Data.Action action) {
        Data.Session currentSession = data.getSession();
        UUID key = UUID.randomUUID();
        Data.Session session = Data.Session
                .builder()
                .action(action)
                .receiptSortType(currentSession.getReceiptSortType())
                .receiptId(currentSession.getReceiptId())
                .currentReceiptPage(currentSession.getCurrentReceiptPage())
                .build();
        return cacheService
                .storeSession(data.getUserId(), key, session)
                .map(ignored -> key);
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
