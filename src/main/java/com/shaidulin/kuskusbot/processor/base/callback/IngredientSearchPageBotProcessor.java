package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.marker.Markers.append;

/**
 * Shows chosen ingredients (if present) and prompts for a new one
 */
@Slf4j
public record IngredientSearchPageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    private static final String INGREDIENTS_BUCKET_LIST = "\n ▫ ";

    @Override
    public Mono<EditMessageText> process(Data data) {
        long userId = data.getUserId();
        return getIngredientsFromCache(userId)
                .filter(ingredients -> !ingredients.isEmpty())
                .map(ingredients -> compileMessage(userId, data.getChatId(), data.getMessageId(),
                        "<u>Выбранные ингредиенты:</u>" + compileIngredientsText(ingredients) + "Напиши ингредиент для поиска"))
                .defaultIfEmpty(compileMessage(userId, data.getChatId(), data.getMessageId(),
                        "Напиши ингредиент для поиска"));
    }

    private Mono<List<String>> getIngredientsFromCache(long userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Getting ingredients from cache");
        }
        return cacheService
                .startSearch(userId)
                .flatMap(ignored -> cacheService.getIngredients(userId));
    }

    private EditMessageText compileMessage(long userId, String chatId, Integer messageId, String text) {
        EditMessageText message = EditMessageText
                .builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .parseMode("HTML")
                .build();
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Compiling message to send: {}", message);
        }
        return message;
    }

    private String compileIngredientsText(List<String> ingredients) {
        return ingredients.stream()
                .map(ingredient -> "<i>" + ingredient + "</i>")
                .collect(Collectors.joining(INGREDIENTS_BUCKET_LIST, INGREDIENTS_BUCKET_LIST, "\n\n"));
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENT_SEARCH_PAGE;
    }
}
