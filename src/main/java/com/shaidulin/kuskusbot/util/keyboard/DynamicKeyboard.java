package com.shaidulin.kuskusbot.util.keyboard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

import static com.shaidulin.kuskusbot.util.keyboard.ButtonConstants.RETURN_TO_RECEIPTS;
import static com.shaidulin.kuskusbot.util.keyboard.ButtonConstants.SHOW_INGREDIENTS_OVERVIEW;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicKeyboard {

    public static final UUID NULL_KEY_UUID = UUID.randomUUID();

    public static InlineKeyboardMarkup createHomePageKeyboard(UUID startSearchButtonKey) {
        List<List<InlineKeyboardButton>> startSearchKeyboard =
                Collections.singletonList(
                        Collections.singletonList(
                                createButton(ButtonConstants.START_SEARCH, startSearchButtonKey)));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(startSearchKeyboard)
                .build();
    }

    public static InlineKeyboardMarkup createIngredientSelectionKeyboard(UUID searchReceiptsButtonKey,
                                                                         UUID searchNextIngredientButtonKey) {
        List<InlineKeyboardButton> receiptSearchButtonRow =
                Collections.singletonList(
                        createButton(ButtonConstants.SEARCH_RECEIPTS, searchReceiptsButtonKey));

        List<InlineKeyboardButton> searchNextIngredientButtonRow =
                Collections.singletonList(
                        createButton(ButtonConstants.SEARCH_NEXT_INGREDIENT, searchNextIngredientButtonKey));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(List.of(receiptSearchButtonRow, searchNextIngredientButtonRow))
                .build();
    }

    public static InlineKeyboardMarkup createSortOptionsChoiceKeyboard(UUID accuracySortOptionButtonKey) {
        List<List<InlineKeyboardButton>> startSearchKeyboard =
                Collections.singletonList(
                        Collections.singletonList(
                                createButton(ButtonConstants.SORT_MOST_ACCURATE, accuracySortOptionButtonKey)));
        return InlineKeyboardMarkup
                .builder()
                .keyboard(startSearchKeyboard)
                .build();
    }

    public static InlineKeyboardMarkup createSuggestionsKeyboard(Map<String, UUID> ingredientButtons,
                                                                 UUID previousPageButtonKey,
                                                                 UUID nextPageButtonKey) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        ingredientButtons.forEach((text, key) -> buttons.add(Collections.singletonList(createButton(text, key))));
        populateNavigationButtons(buttons, previousPageButtonKey, nextPageButtonKey);

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    public static InlineKeyboardMarkup createReceiptPresentationKeyboard(UUID receiptIngredientsButtonKey,
                                                                         UUID previousPageButtonKey,
                                                                         UUID nextPageButtonKey) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(Collections.singletonList(createButton(SHOW_INGREDIENTS_OVERVIEW, receiptIngredientsButtonKey)));
        populateNavigationButtons(buttons, previousPageButtonKey, nextPageButtonKey);

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    public static InlineKeyboardMarkup createReceiptKeyboard(UUID receiptPresentationPageButtonKey,
                                                             Map<String, UUID> optionButtons) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        optionButtons.forEach((text, action) -> buttons.add(Collections.singletonList(createButton(text, action))));
        buttons.add(Collections.singletonList(createButton(RETURN_TO_RECEIPTS, receiptPresentationPageButtonKey)));

        return InlineKeyboardMarkup
                .builder()
                .keyboard(buttons)
                .build();
    }

    private static InlineKeyboardButton createButton(String text, UUID key) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(key.toString())
                .build();
    }

    private static void populateNavigationButtons(List<List<InlineKeyboardButton>> buttons,
                                                  UUID previousPageButtonKey,
                                                  UUID nextPageButtonKey) {
        InlineKeyboardButton toPreviousPageButton = !Objects.equals(NULL_KEY_UUID, previousPageButtonKey)
                ? createButton("⬅", previousPageButtonKey)
                : null;

        InlineKeyboardButton toNextPageButton = !Objects.equals(NULL_KEY_UUID, nextPageButtonKey)
                ? createButton("➡", nextPageButtonKey)
                : null;

        createNavigationPanelRow(toPreviousPageButton, toNextPageButton).ifPresent(buttons::add);
    }

    private static Optional<List<InlineKeyboardButton>> createNavigationPanelRow(InlineKeyboardButton toPreviousPageButton,
                                                                                 InlineKeyboardButton toNextPageButton) {
        List<InlineKeyboardButton> navigationPanelRow = new ArrayList<>();

        if (toPreviousPageButton != null) {
            navigationPanelRow.add(toPreviousPageButton);
        }
        if (toNextPageButton != null) {
            navigationPanelRow.add(toNextPageButton);
        }
        return !navigationPanelRow.isEmpty() ? Optional.of(navigationPanelRow) : Optional.empty();
    }
}
