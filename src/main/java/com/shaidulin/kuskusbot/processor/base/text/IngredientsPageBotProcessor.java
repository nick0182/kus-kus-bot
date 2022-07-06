package com.shaidulin.kuskusbot.processor.base.text;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.IngredientPageKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.TreeSet;

import static net.logstash.logback.marker.Markers.append;

/**
 * Shows the first page's offering of 3 possible ingredients
 */
@Slf4j
public record IngredientsPageBotProcessor(StringCacheService cacheService, ReceiptService receiptService,
                                          IngredientPageKeyboardProvider keyboardProvider)
        implements BaseBotProcessor {

    @Override
    public Mono<SendMessage> process(Data data) {
        long userId = data.getUserId();
        String chatId = data.getChatId();
        return getIngredientsFromCache(userId)
                .flatMap(known -> receiptService.suggestIngredients(data.getInput(), known))
                .map(ingredientMatch -> unwrapIngredients(userId, ingredientMatch))
                .filterWhen(ingredients -> storeIngredientsInCache(userId, ingredients))
                .flatMap(ingredients -> keyboardProvider.compileKeyboard(userId, 0, ingredients))
                .map(ingredientsKeyboard -> SendMessage
                        .builder()
                        .text("Вот что смог найти")
                        .chatId(chatId)
                        .replyMarkup(ingredientsKeyboard)
                        .build()
                )
                .onErrorReturn(IllegalArgumentException.class,
                        SendMessage
                                .builder()
                                .text("Ничего не нашел \uD83E\uDD14 Попробуй еще раз")
                                .chatId(chatId)
                                .build()
                );
    }

    private Mono<List<String>> getIngredientsFromCache(long userId) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Getting ingredients from cache");
        }
        return cacheService
                .startSearch(userId)
                .flatMap(ignored -> cacheService.getIngredients(userId));
    }

    private TreeSet<IngredientValue> unwrapIngredients(long userId, IngredientMatch ingredientMatch) {
        if (ingredientMatch.ingredients().isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace(append("user_id", userId), "No ingredients found");
            }
            throw new IllegalArgumentException(); // FIXME create custom exception
        } else {
            return ingredientMatch.ingredients();
        }
    }

    private Mono<Boolean> storeIngredientsInCache(long userId, TreeSet<IngredientValue> ingredients) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Storing ingredients in cache. Ingredients: {}", ingredients);
        }
        return cacheService.storeIngredientSuggestions(userId, ingredients);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENTS_PAGE;
    }
}
