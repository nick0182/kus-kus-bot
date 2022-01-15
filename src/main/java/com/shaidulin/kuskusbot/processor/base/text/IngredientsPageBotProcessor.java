package com.shaidulin.kuskusbot.processor.base.text;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import static com.shaidulin.kuskusbot.util.keyboard.ButtonConstants.*;

/**
 * Shows the first page's offering of 3 possible ingredients
 */
public record IngredientsPageBotProcessor(StringCacheService cacheService, ReceiptService receiptService)
        implements BaseBotProcessor {

    @Override
    public Mono<SendMessage> process(Data data) {
        String userId = data.getUserId();
        String toSearch = data.getInput();
        return cacheService
                .getIngredients(userId)
                .flatMap(known -> receiptService.suggestIngredients(toSearch, known))
                .map(ingredientMatch -> {
                    if (ingredientMatch.ingredients().isEmpty()) {
                        throw new IllegalArgumentException(); // FIXME create custom exception
                    } else {
                        return ingredientMatch.ingredients();
                    }
                })
                .filterWhen(ingredientMatch -> cacheService.storeIngredientSuggestions(userId, ingredientMatch))
                .flatMap(ingredients -> compileIngredientButtons(userId, ingredients))
                .zipWhen(ingredientButtons -> hasNextPageButton(ingredientButtons.size(), userId),
                        (ingredientButtons, buttonKey) -> DynamicKeyboard.createSuggestionsKeyboard(
                                ingredientButtons, DynamicKeyboard.NULL_KEY_UUID, buttonKey))
                .map(ingredientsKeyboard -> SendMessage
                        .builder()
                        .text(INGREDIENTS_SEARCH_RESULT_MESSAGE)
                        .chatId(userId)
                        .replyMarkup(ingredientsKeyboard)
                        .build()
                )
                .onErrorReturn(IllegalArgumentException.class,
                        SendMessage
                                .builder()
                                .text(INGREDIENTS_EMPTY_SEARCH_RESULT_MESSAGE)
                                .chatId(userId)
                                .build()
                );
    }

    private Mono<Map<String, UUID>> compileIngredientButtons(String userId, TreeSet<IngredientValue> ingredients) {
        Map<String, UUID> ingredientButtons = new LinkedHashMap<>();
        Map<UUID, Data.Session> sessions = new LinkedHashMap<>();
        ingredients
                .stream()
                .limit(INGREDIENTS_PAGE_SIZE)
                .forEach(ingredient -> {
                    UUID key = UUID.randomUUID();
                    String name = ingredient.name();
                    int count = ingredient.count();
                    ingredientButtons.put(String.join(" - ", name, String.valueOf(count)), key);
                    sessions.put(key, Data.Session
                            .builder()
                            .action(Data.Action.SHOW_SEARCH_CONFIGURATION_OPTIONS)
                            .ingredientName(name)
                            .build());
                });
        return cacheService.storeSession(userId, sessions).map(ignored -> ingredientButtons);
    }

    private Mono<UUID> hasNextPageButton(int ingredientsCount, String userId) {
        if (ingredientsCount > INGREDIENTS_PAGE_SIZE) {
            UUID key = UUID.randomUUID();
            Data.Session session = Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(1)
                    .build();
            return cacheService
                    .storeSession(userId, key, session)
                    .map(ignored -> key);
        } else {
            return Mono.just(DynamicKeyboard.NULL_KEY_UUID);
        }
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENTS_PAGE;
    }
}
