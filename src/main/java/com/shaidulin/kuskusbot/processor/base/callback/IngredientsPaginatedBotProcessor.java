package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import static com.shaidulin.kuskusbot.util.keyboard.ButtonConstants.INGREDIENTS_PAGE_SIZE;

/**
 * Shows a paginated offering of 3 possible ingredients
 */
public record IngredientsPaginatedBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageReplyMarkup> process(Data data) {
        return cacheService
                .getIngredientSuggestions(data.getUserId())
                .flatMap(ingredients -> compileIngredientButtons(data, ingredients))
                .zipWith(compilePreviousPageButton(data))
                .zipWhen(tuple2 -> compileNextPageButton(tuple2.getT1().size(), data),
                        (tuple2, buttonKey) -> DynamicKeyboard.createSuggestionsKeyboard(tuple2.getT1(), tuple2.getT2(), buttonKey))
                .map(ingredientsKeyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .replyMarkup(ingredientsKeyboard)
                        .build());
    }

    private Mono<Map<String, UUID>> compileIngredientButtons(Data data, TreeSet<IngredientValue> ingredients) {
        long shownCount = (long) data.getSession().getCurrentIngredientsPage() * INGREDIENTS_PAGE_SIZE;

        Map<String, UUID> ingredientButtons = new LinkedHashMap<>();
        Map<UUID, Data.Session> sessions = new LinkedHashMap<>();
        ingredients
                .stream()
                .skip(shownCount)
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
        return cacheService.storeSession(data.getUserId(), sessions).map(ignored -> ingredientButtons);
    }

    private Mono<UUID> compilePreviousPageButton(Data data) {
        int page = data.getSession().getCurrentIngredientsPage();
        if (page > 0) {
            UUID key = UUID.randomUUID();
            Data.Session session = Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(page - 1)
                    .build();
            return cacheService.storeSession(data.getUserId(), key, session).map(ignored -> key);
        } else {
            return Mono.just(DynamicKeyboard.NULL_KEY_UUID);
        }
    }

    private Mono<UUID> compileNextPageButton(int ingredientsCount, Data data) {
        int page = data.getSession().getCurrentIngredientsPage();
        long shownCount = (long) page * INGREDIENTS_PAGE_SIZE;
        if (ingredientsCount > shownCount + INGREDIENTS_PAGE_SIZE) {
            UUID key = UUID.randomUUID();
            Data.Session session = Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(page + 1)
                    .build();
            return cacheService
                    .storeSession(data.getUserId(), key, session)
                    .map(ignored -> key);
        } else {
            return Mono.just(DynamicKeyboard.NULL_KEY_UUID);
        }
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENTS_PAGINATED;
    }
}
