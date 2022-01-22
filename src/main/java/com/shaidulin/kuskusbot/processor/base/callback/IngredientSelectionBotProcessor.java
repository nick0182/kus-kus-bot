package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Shows chosen ingredients and offers:
 * 1. to start searching for receipts
 * 2. to start searching for receipts OR to add another ingredient
 */
public record IngredientSelectionBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageText> process(Data data) {
        String userId = data.getUserId();
        return cacheService
                .storeIngredient(userId, data.getSession().getIngredientName())
                .flatMap(ignored -> cacheService.getIngredients(userId))
                .zipWith(compileButtons(userId))
                .map(tuple2 -> EditMessageText
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .text("Выбранные ингредиенты: " + tuple2.getT1())
                        .replyMarkup(tuple2.getT2())
                        .build()
                );
    }

    private Mono<InlineKeyboardMarkup> compileButtons(String userId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Искать!", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, Data.Session.builder().action(Data.Action.SHOW_SORT_OPTIONS).build());
        buttonIndex++;
        buttons.add(DynamicKeyboard.createButtonRow( "Добавить ингредиент", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, Data.Session.builder().action(Data.Action.PROMPT_INGREDIENT).build());
        return cacheService
                .storeSession(userId, sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENT_SELECTION;
    }
}
