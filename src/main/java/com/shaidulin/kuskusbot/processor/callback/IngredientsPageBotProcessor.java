package com.shaidulin.kuskusbot.processor.callback;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public class IngredientsPageBotProcessor extends BotProcessor {

    public IngredientsPageBotProcessor(LettuceCacheService lettuceCacheService) {
        super(lettuceCacheService);
    }

    @Override
    public Mono<EditMessageReplyMarkup> process(Update update) {
        CallbackQuery userCallbackQuery = update.getCallbackQuery();
        String userId = userCallbackQuery.getFrom().getId().toString();
        String chatId = userCallbackQuery.getMessage().getChatId().toString();
        Integer messageId = userCallbackQuery.getMessage().getMessageId();
        int page = Integer.parseInt(userCallbackQuery.getData());
        return lettuceCacheService
                .getIngredientSearchStep(userId)
                .flatMap(searchStep -> lettuceCacheService.getIngredientSuggestions(userId, searchStep))
                .map(ingredients -> KeyboardCreator.createSuggestionsKeyboard(ingredients, page))
                .map(ingredientsKeyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .replyMarkup(ingredientsKeyboard)
                        .build());
    }

    @Override
    public UpdateKey getKey() {
        return UpdateKey.USER_INGREDIENTS_PAGE;
    }
}
