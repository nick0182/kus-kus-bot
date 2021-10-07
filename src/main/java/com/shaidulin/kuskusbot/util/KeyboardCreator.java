package com.shaidulin.kuskusbot.util;

import com.shaidulin.kuskusbot.dto.IngredientValue;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class KeyboardCreator {

    public static InlineKeyboardMarkup createSuggestionsKeyboard(TreeSet<IngredientValue> ingredients, int page) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        if (page > 0) {
            buttons.add(createButtonRow("Предыдущие", String.valueOf(page - 1)));
        }

        final int pageSize = 3;

        long shownCount = (long) page * pageSize;

        ingredients
                .stream()
                .skip(shownCount)
                .limit(pageSize)
                .forEach(ingredient -> buttons.add(createButtonRow(createButtonText(ingredient), ingredient.getName())));

        if (ingredients.size() > shownCount + pageSize) {
            buttons.add(createButtonRow("Ещё", String.valueOf(page + 1)));
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
