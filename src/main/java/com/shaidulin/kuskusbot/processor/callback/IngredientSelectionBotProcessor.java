package com.shaidulin.kuskusbot.processor.callback;

import com.shaidulin.kuskusbot.processor.BotProcessor;
import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.update.UpdateKey;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public class IngredientSelectionBotProcessor extends BotProcessor {

    public IngredientSelectionBotProcessor(LettuceCacheService lettuceCacheService) {
        super(lettuceCacheService);
    }

    @Override
    public Mono<EditMessageText> process(Update update) {
        CallbackQuery userCallbackQuery = update.getCallbackQuery();
        String userId = userCallbackQuery.getFrom().getId().toString();
        Message message = userCallbackQuery.getMessage();
        String chatId = message.getChatId().toString();
        Integer messageId = message.getMessageId();
        String selectedIngredient = userCallbackQuery.getData();
        return lettuceCacheService
                .storeIngredient(userId, selectedIngredient)
                .flatMap(ignored -> lettuceCacheService.getIngredients(userId))
                .map(ingredients -> EditMessageText
                        .builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Выбранные ингредиенты: " + ingredients)
                        .replyMarkup(ingredients.size() != 3
                                ? ButtonConstants.twoOptionsChoiceKeyboard
                                : ButtonConstants.oneOptionChoiceKeyboard)
                        .build()
                );
    }

    @Override
    public UpdateKey getKey() {
        return UpdateKey.USER_INGREDIENT_SELECTION;
    }
}
