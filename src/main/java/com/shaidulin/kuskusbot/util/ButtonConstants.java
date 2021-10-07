package com.shaidulin.kuskusbot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;
import java.util.List;

public final class ButtonConstants {

    private ButtonConstants() {}

    public static final String SEARCH_RECEIPTS = "Искать!";

    public static final String SEARCH_NEXT_INGREDIENT = "Добавить ингредиент";

    public static final String START_SEARCH = "Начать поиск";

    public static final InlineKeyboardMarkup startSearchKeyboard;

    public static final InlineKeyboardMarkup oneOptionChoiceKeyboard;

    public static final InlineKeyboardMarkup twoOptionsChoiceKeyboard;

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
    }
}
