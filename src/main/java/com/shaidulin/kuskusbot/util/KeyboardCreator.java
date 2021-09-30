package com.shaidulin.kuskusbot.util;

import com.shaidulin.kuskusbot.dto.IngredientValue;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class KeyboardCreator {

    private static final int PAGE_SIZE = 3;

    private static final String SHOW_MORE_SUGGESTIONS = "Ещё";

    private static final String SHOW_LESS_SUGGESTIONS = "Предыдущие";

    public static InlineKeyboardMarkup createSuggestionsKeyboard(TreeSet<IngredientValue> ingredients, int page) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        if (page > 0) {
            buttons.add(createButtonRow(SHOW_LESS_SUGGESTIONS, String.valueOf(page - 1)));
        }

        ingredients
                .stream()
                .limit(3)
                .forEach(ingredient -> buttons.add(createButtonRow(createButtonText(ingredient), ingredient.getName())));

        if (ingredients.size() > PAGE_SIZE) {
            buttons.add(createButtonRow(SHOW_MORE_SUGGESTIONS, String.valueOf(page + 1)));
        }

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    private static List<InlineKeyboardButton> createButtonRow(String text, String data) {
        return Collections.singletonList(
                InlineKeyboardButton
                        .builder()
                        .text(text)
                        .callbackData(data)
                        .build());
    }

    private static String createButtonText(IngredientValue ingredient) {
        return String.join(" - ", ingredient.getName(), String.valueOf(ingredient.getCount()));
    }
}
