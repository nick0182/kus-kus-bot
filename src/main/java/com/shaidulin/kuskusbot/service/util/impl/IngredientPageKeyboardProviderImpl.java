package com.shaidulin.kuskusbot.service.util.impl;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.IngredientPageKeyboardProvider;
import com.shaidulin.kuskusbot.service.util.KeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.IntStream;

import static net.logstash.logback.marker.Markers.append;

@Slf4j
public class IngredientPageKeyboardProviderImpl extends KeyboardProvider implements IngredientPageKeyboardProvider {

    private static final int INGREDIENTS_PAGE_SIZE = 3;

    public IngredientPageKeyboardProviderImpl(StringCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public Mono<InlineKeyboardMarkup> compileKeyboard(long userId, int page, TreeSet<IngredientValue> ingredients) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int shownCount = page * INGREDIENTS_PAGE_SIZE;
        if (shownCount != 0) {
            // skip previous page(s)
            IntStream.range(0, shownCount).forEach(index -> ingredients.pollFirst());
        }
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Got ingredient suggestions: {}", ingredients);
        }

        int buttonCurrentIndex = 0;
        IngredientValue ingredient;
        while ((ingredient = ingredients.pollFirst()) != null && buttonCurrentIndex < INGREDIENTS_PAGE_SIZE) {
            String name = ingredient.name();
            int count = ingredient.count();
            String text = String.join(" - ", name, String.valueOf(count));
            buttons.add(DynamicKeyboard.createButtonRow(text, String.valueOf(buttonCurrentIndex)));
            sessionHash.put(buttonCurrentIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_SEARCH_CONFIGURATION_OPTIONS)
                    .ingredientName(name)
                    .build());
            ++buttonCurrentIndex;
        }

        Integer previousPageButtonIndex = null;
        Integer nextPageButtonIndex = null;

        if (page != 0) {
            previousPageButtonIndex = ++buttonCurrentIndex;
            sessionHash.put(previousPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(page - 1)
                    .build());
        }

        if (!CollectionUtils.isEmpty(ingredients)) {
            nextPageButtonIndex = ++buttonCurrentIndex;
            sessionHash.put(nextPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(page + 1)
                    .build());
        }

        buttons.add(DynamicKeyboard.createNavigationPanelRow(previousPageButtonIndex, nextPageButtonIndex));

        return storeSession(userId, sessionHash).thenReturn(InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }
}
