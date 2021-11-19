package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import com.shaidulin.kuskusbot.util.KeyboardCreator;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

/**
 * Shows a paginated offering of 3 possible ingredients
 */
public record IngredientsPaginatedBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageReplyMarkup> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        int page = Integer.parseInt(callbackWrapper.data());
        return cacheService
                .getIngredientSuggestions(callbackWrapper.userId())
                .map(ingredients -> KeyboardCreator.createSuggestionsKeyboard(ingredients, page))
                .map(ingredientsKeyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(callbackWrapper.chatId())
                        .messageId(callbackWrapper.messageId())
                        .replyMarkup(ingredientsKeyboard)
                        .build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENTS_PAGINATED;
    }
}
