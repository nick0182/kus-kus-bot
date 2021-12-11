package com.shaidulin.kuskusbot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;

public final class ButtonConstants {

    private ButtonConstants() {
    }

    // -------------------- button text ----------------------------

    public static final String SEARCH_RECEIPTS = "Искать!";

    public static final String SEARCH_NEXT_INGREDIENT = "Добавить ингредиент";

    public static final String START_SEARCH = "Начать поиск";

    public static final String SHOW_RECEIPT = "Показать рецепт";

    public static final String SORT_MOST_ACCURATE = "Самые точные";

    public static final String RETURN_TO_RECEIPTS = "↩";

    // -------------------- button payload prefixes ----------------------------

    public static final String INGREDIENTS_PAGE_PAYLOAD_PREFIX = "Ing";

    public static final String RECEIPTS_PAGE_PAYLOAD_PREFIX = "Rec";

    public static final String RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX = "RecIng";

    // -------------------- keyboards ----------------------------

    public static final InlineKeyboardMarkup startSearchKeyboard;

    public static final InlineKeyboardMarkup oneOptionChoiceKeyboard;

    public static final InlineKeyboardMarkup twoOptionsChoiceKeyboard;

    public static final InlineKeyboardMarkup sortAccurateChoiceKeyboard;

    static {
        List<InlineKeyboardButton> startSearchKeyboardRow =
                Collections.singletonList(InlineKeyboardButton
                        .builder()
                        .text(START_SEARCH)
                        .callbackData(START_SEARCH)
                        .build());

        List<InlineKeyboardButton> receiptSearchButtonRow =
                Collections.singletonList(InlineKeyboardButton
                        .builder()
                        .text(SEARCH_RECEIPTS)
                        .callbackData(SEARCH_RECEIPTS)
                        .build());

        List<InlineKeyboardButton> searchNextIngredientButtonRow =
                Collections.singletonList(InlineKeyboardButton
                        .builder()
                        .text(SEARCH_NEXT_INGREDIENT)
                        .callbackData(SEARCH_NEXT_INGREDIENT)
                        .build());

        List<InlineKeyboardButton> sortAccurateChoiceButtonRow =
                Collections.singletonList(InlineKeyboardButton
                        .builder()
                        .text(SORT_MOST_ACCURATE)
                        .callbackData(SortType.ACCURACY.name())
                        .build());

        startSearchKeyboard = InlineKeyboardMarkup
                .builder()
                .keyboard(Collections.singletonList(startSearchKeyboardRow))
                .build();

        oneOptionChoiceKeyboard = InlineKeyboardMarkup
                .builder()
                .keyboard(Collections.singletonList(receiptSearchButtonRow))
                .build();

        twoOptionsChoiceKeyboard = InlineKeyboardMarkup
                .builder()
                .keyboard(List.of(receiptSearchButtonRow, searchNextIngredientButtonRow))
                .build();

        sortAccurateChoiceKeyboard = InlineKeyboardMarkup
                .builder()
                .keyboard(Collections.singletonList(sortAccurateChoiceButtonRow))
                .build();
    }
}
