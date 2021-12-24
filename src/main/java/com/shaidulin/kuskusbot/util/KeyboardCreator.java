package com.shaidulin.kuskusbot.util;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

import static com.shaidulin.kuskusbot.util.ButtonConstants.*;

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

        createNavigationPanelRow(INGREDIENTS_PAGE_PAYLOAD_PREFIX, page, ingredients.size() > shownCount + pageSize)
                .ifPresent(buttons::add);

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    public static InlineKeyboardMarkup createReceiptPresentationKeyboard(int page, int queryParam, boolean hasMore) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(Collections.singletonList(createButton(
                SHOW_INGREDIENTS,
                createReceiptIdentifier(queryParam, page, RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX))));

        createNavigationPanelRow(RECEIPTS_PAGE_PAYLOAD_PREFIX, page, hasMore).ifPresent(buttons::add);

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    public static InlineKeyboardMarkup createReceiptKeyboard(int queryParam, int page, Map<String, String> buttonsMap) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttonsMap.forEach((text, data) -> buttons.add(
                Collections.singletonList(createButton(text, createReceiptIdentifier(queryParam, page, data)))));

        buttons.add(Collections.singletonList(
                createButton(RETURN_TO_RECEIPTS, createPageIdentifier(RECEIPTS_PAGE_PAYLOAD_PREFIX, page))));

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

    private static Optional<List<InlineKeyboardButton>> createNavigationPanelRow(String payloadIdentifier,
                                                                                 int page, boolean hasMore) {
        List<InlineKeyboardButton> navigationPanelRow = new ArrayList<>();

        Optional<InlineKeyboardButton> toPreviousPageButton = page > 0
                ? Optional.of(createButton("⬅", createPageIdentifier(payloadIdentifier, page - 1)))
                : Optional.empty();

        Optional<InlineKeyboardButton> toNextPageButton = hasMore
                ? Optional.of(createButton("➡", createPageIdentifier(payloadIdentifier, page + 1)))
                : Optional.empty();

        toPreviousPageButton.ifPresent(navigationPanelRow::add);
        toNextPageButton.ifPresent(navigationPanelRow::add);

        return !navigationPanelRow.isEmpty() ? Optional.of(navigationPanelRow) : Optional.empty();
    }

    private static String createButtonText(IngredientValue ingredient) {
        return String.join(" - ", ingredient.name(), String.valueOf(ingredient.count()));
    }

    private static String createPageIdentifier(String payloadIdentifier, int page) {
        return String.join("_", payloadIdentifier, String.valueOf(page));
    }

    private static String createReceiptIdentifier(int queryParam, int page, String payloadPrefix) {
        return String.join("_", payloadPrefix,
                String.valueOf(queryParam), String.valueOf(page));
    }
}
