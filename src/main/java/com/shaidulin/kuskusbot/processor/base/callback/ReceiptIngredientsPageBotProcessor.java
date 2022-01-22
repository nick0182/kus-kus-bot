package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.receipt.Ingredient;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;
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
                .zipWith(compileButtons(data))
                .map(tuple2 -> EditMessageCaption.builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .caption(createIngredientsCaption(tuple2.getT1().ingredients()))
                        .replyMarkup(tuple2.getT2())
                        .build());
    }

    private Mono<InlineKeyboardMarkup> compileButtons(Data data) {
        Data.Session currentSession = data.getSession();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow( "Энергетическая ценность", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, createNewSession(currentSession, Data.Action.SHOW_RECEIPT_NUTRITION_PAGE));
        buttonIndex++;
        buttons.add(DynamicKeyboard.createButtonRow( "Как готовить", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, createNewSession(currentSession, Data.Action.SHOW_STEP_PAGE));
        buttonIndex++;
        buttons.add(DynamicKeyboard.createButtonRow( "↩", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, createNewSession(currentSession, Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE));
        return cacheService
                .storeSession(data.getUserId(), sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    private Data.Session createNewSession(Data.Session currentSession, Data.Action action) {
        return Data.Session
                .builder()
                .action(action)
                .receiptSortType(currentSession.getReceiptSortType())
                .receiptId(currentSession.getReceiptId())
                .currentReceiptPage(currentSession.getCurrentReceiptPage())
                .build();
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
