package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

/**
 * Shows chosen ingredients (if present) and prompts for a new one
 */
public record IngredientSearchPageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageText> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        return cacheService
                .startSearch(callbackWrapper.userId())
                .flatMap(ignored -> cacheService.getIngredients(callbackWrapper.userId()))
                .map(ingredients -> {
                    if (ingredients.isEmpty()) {
                        throw new IllegalArgumentException(); // FIXME create custom exception
                    } else {
                        return ingredients;
                    }
                })
                .map(ingredients -> EditMessageText
                        .builder()
                        .chatId(callbackWrapper.chatId())
                        .messageId(callbackWrapper.messageId())
                        .text("Выбранные ингредиенты: " + ingredients + "\n\nНапиши ингредиент для поиска")
                        .build())
                .onErrorReturn(IllegalArgumentException.class,
                        EditMessageText
                                .builder()
                                .chatId(callbackWrapper.chatId())
                                .messageId(callbackWrapper.messageId())
                                .text("Напиши ингредиент для поиска")
                                .build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENT_SEARCH_PAGE;
    }
}
