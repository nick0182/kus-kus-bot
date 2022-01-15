package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.UUID;

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
                .zipWith(compileButton(userId, Data.Action.SHOW_SORT_OPTIONS))
                .zipWhen(tuple2 -> compileButton(userId, Data.Action.PROMPT_INGREDIENT),
                        (tuple2, buttonKey) ->
                                Tuples.of(tuple2.getT1(),
                                        DynamicKeyboard.createIngredientSelectionKeyboard(tuple2.getT2(), buttonKey)))
                .map(tuple2 -> EditMessageText
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .text("Выбранные ингредиенты: " + tuple2.getT1())
                        .replyMarkup(tuple2.getT2())
                        .build()
                );
    }

    private Mono<UUID> compileButton(String userId, Data.Action action) {
        UUID key = UUID.randomUUID();
        return cacheService
                .storeSession(userId, key, Data.Session.builder().action(action).build())
                .map(ignored -> key);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENT_SELECTION;
    }
}
