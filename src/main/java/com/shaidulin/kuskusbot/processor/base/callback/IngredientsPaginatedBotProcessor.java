package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.IngredientPageKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import reactor.core.publisher.Mono;

import java.util.TreeSet;

import static net.logstash.logback.marker.Markers.append;

/**
 * Shows a paginated offering of 3 possible ingredients
 */
@Slf4j
public record IngredientsPaginatedBotProcessor(StringCacheService cacheService,
                                               IngredientPageKeyboardProvider keyboardProvider) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageReplyMarkup> process(Data data) {
        int page = data.getSession().getCurrentIngredientsPage();
        String userId = data.getUserId();
        return getIngredientSuggestionsFromCache(userId)
                .flatMap(ingredients -> keyboardProvider.compileKeyboard(userId, page, ingredients))
                .map(ingredientsKeyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .replyMarkup(ingredientsKeyboard)
                        .build());
    }

    private Mono<TreeSet<IngredientValue>> getIngredientSuggestionsFromCache(String userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Getting ingredient suggestions from cache");
        }
        return cacheService.getIngredientSuggestions(userId);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENTS_PAGINATED;
    }
}
