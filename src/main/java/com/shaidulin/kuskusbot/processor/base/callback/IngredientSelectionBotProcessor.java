package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

/**
 * Shows chosen ingredients and offers:
 * 1. to start searching for receipts
 * 2. to start searching for receipts OR to add another ingredient
 */
public record IngredientSelectionBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageText> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        return cacheService
                .storeIngredient(callbackWrapper.userId(), callbackWrapper.data())
                .flatMap(ignored -> cacheService.getIngredients(callbackWrapper.userId()))
                .map(ingredients -> EditMessageText
                        .builder()
                        .chatId(callbackWrapper.chatId())
                        .messageId(callbackWrapper.messageId())
                        .text("Выбранные ингредиенты: " + ingredients)
                        .replyMarkup(ingredients.size() != 3
                                ? ButtonConstants.twoOptionsChoiceKeyboard
                                : ButtonConstants.oneOptionChoiceKeyboard)
                        .build()
                );
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENT_SELECTION;
    }
}
