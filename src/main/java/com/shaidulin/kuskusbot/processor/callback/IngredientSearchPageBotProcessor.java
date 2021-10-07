package com.shaidulin.kuskusbot.processor.callback;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public class IngredientSearchPageBotProcessor extends BotProcessor {

    public IngredientSearchPageBotProcessor(LettuceCacheService lettuceCacheService) {
        super(lettuceCacheService);
    }

    @Override
    public Mono<EditMessageText> process(Update update) {
        CallbackQuery userCallbackQuery = update.getCallbackQuery();
        String userId = userCallbackQuery.getFrom().getId().toString();
        Message message = userCallbackQuery.getMessage();
        String chatId = message.getChatId().toString();
        Integer messageId = message.getMessageId();
        return lettuceCacheService
                .startSearch(userId)
                .flatMap(ignored -> lettuceCacheService.getIngredients(userId))
                .map(ingredients -> {
                    if (ingredients.isEmpty()) {
                        throw new IllegalArgumentException(); // FIXME create custom exception
                    } else {
                        return ingredients;
                    }
                })
                .map(ingredients -> EditMessageText
                        .builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Выбранные ингредиенты: " + ingredients + "\n\nНапиши ингредиент для поиска")
                        .build())
                .onErrorReturn(IllegalArgumentException.class,
                        EditMessageText
                                .builder()
                                .chatId(chatId)
                                .messageId(messageId)
                                .text("Напиши ингредиент для поиска")
                                .build());
    }

    @Override
    public UpdateKey getKey() {
        return UpdateKey.SEARCH_PAGE;
    }
}
