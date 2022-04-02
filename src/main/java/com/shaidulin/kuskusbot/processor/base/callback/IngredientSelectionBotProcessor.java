package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.SimpleKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import reactor.core.publisher.Mono;

import java.util.List;

import static net.logstash.logback.marker.Markers.append;

/**
 * Shows chosen ingredients and offers:
 * 1. to start searching for receipts
 * 2. to start searching for receipts OR to add another ingredient
 */
@Slf4j
public record IngredientSelectionBotProcessor(StringCacheService cacheService,
                                              SimpleKeyboardProvider keyboardProvider) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageText> process(Data data) {
        String userId = data.getUserId();
        return storeIngredientInCache(userId, data.getSession().getIngredientName())
                .flatMap(ignored -> getIngredientsFromCache(userId))
                .zipWith(keyboardProvider.compileKeyboard(userId))
                .map(tuple2 -> EditMessageText
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .text("Выбранные ингредиенты: " + tuple2.getT1())
                        .replyMarkup(tuple2.getT2())
                        .build()
                );
    }

    private Mono<String> storeIngredientInCache(String userId, String ingredient) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Storing ingredient in cache: {}", ingredient);
        }
        return cacheService.storeIngredient(userId, ingredient);
    }

    private Mono<List<String>> getIngredientsFromCache(String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Getting ingredients from cache");
        }
        return cacheService.getIngredients(userId);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENT_SELECTION;
    }
}
