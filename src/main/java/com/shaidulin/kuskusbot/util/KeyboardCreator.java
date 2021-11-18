package com.shaidulin.kuskusbot.util;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

import static com.shaidulin.kuskusbot.util.ButtonConstants.SHOW_RECEIPT_INGREDIENTS;

public class KeyboardCreator {

    public static InlineKeyboardMarkup createSuggestionsKeyboard(TreeSet<IngredientValue> ingredients, int page) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        final int pageSize = 3;

        long shownCount = (long) page * pageSize;

        ingredients
                .stream()
                .skip(shownCount)
                .limit(pageSize)
                .forEach(ingredient -> buttons.add(
                        Collections.singletonList(createButton(createButtonText(ingredient), ingredient.name()))));

        createNavigationPanelRow(page, ingredients.size() > shownCount + pageSize).ifPresent(buttons::add);

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    public static InlineKeyboardMarkup createReceiptKeyboard(List<ReceiptPresentationValue> receipts, int page) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        final int pageSize = 1;

        long shownCount = (long) page * pageSize;

        buttons.add(Collections.singletonList(createButton(SHOW_RECEIPT_INGREDIENTS, SHOW_RECEIPT_INGREDIENTS)));

        createNavigationPanelRow(page, receipts.size() > shownCount + pageSize).ifPresent(buttons::add);

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    private static InlineKeyboardButton createButton(String text, String data) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(data)
                .build();
    }

    private static String createButtonText(IngredientValue ingredient) {
        return String.join(" - ", ingredient.name(), String.valueOf(ingredient.count()));
    }

    private static Optional<List<InlineKeyboardButton>> createNavigationPanelRow(int page, boolean hasMore) {
        List<InlineKeyboardButton> navigationPanelRow = new ArrayList<>();

        Optional<InlineKeyboardButton> toPreviousPageButton = page > 0
                ? Optional.of(createButton("⬅", String.valueOf(page - 1)))
                : Optional.empty();

        Optional<InlineKeyboardButton> toNextPageButton = hasMore
                ? Optional.of(createButton("➡", String.valueOf(page + 1)))
                : Optional.empty();

        toPreviousPageButton.ifPresent(navigationPanelRow::add);
        toNextPageButton.ifPresent(navigationPanelRow::add);

        return !navigationPanelRow.isEmpty() ? Optional.of(navigationPanelRow) : Optional.empty();
    }
}
