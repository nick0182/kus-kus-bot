package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import reactor.core.publisher.Mono;

/**
 * Shows chosen ingredients (if present) and prompts for a new one
 */
public record IngredientSearchPageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageText> process(Data data) {
        return cacheService
                .startSearch(data.getUserId())
                .flatMap(ignored -> cacheService.getIngredients(data.getUserId()))
                .map(ingredients -> {
                    if (ingredients.isEmpty()) {
                        throw new IllegalArgumentException(); // FIXME create custom exception
                    } else {
                        return ingredients;
                    }
                })
                .map(ingredients -> EditMessageText
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .text("Выбранные ингредиенты: " + ingredients + "\n\nНапиши ингредиент для поиска")
                        .build())
                .onErrorReturn(IllegalArgumentException.class,
                        EditMessageText
                                .builder()
                                .chatId(data.getChatId())
                                .messageId(data.getMessageId())
                                .text("Напиши ингредиент для поиска")
                                .build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENT_SEARCH_PAGE;
    }
}
