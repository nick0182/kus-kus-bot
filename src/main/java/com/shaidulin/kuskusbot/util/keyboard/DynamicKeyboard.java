package com.shaidulin.kuskusbot.util.keyboard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicKeyboard {

    public static List<InlineKeyboardButton> createButtonRow(String text, String data) {
        return Collections.singletonList(createButton(text, data));
    }

    public static List<InlineKeyboardButton> createNavigationPanelRow(Integer previousPageButtonIndex,
                                                                      Integer nextPageButtonIndex) {
        List<InlineKeyboardButton> navigationPanelRow = new ArrayList<>();
        Optional.ofNullable(previousPageButtonIndex).ifPresent(addNavigationButton(navigationPanelRow, "⬅"));
        Optional.ofNullable(nextPageButtonIndex).ifPresent(addNavigationButton(navigationPanelRow, "➡"));
        return navigationPanelRow;
    }

    private static Consumer<Integer> addNavigationButton(List<InlineKeyboardButton> navigationPanelRow, String text) {
        return index -> navigationPanelRow.add(createButton(text, String.valueOf(index)));
    }

    private static InlineKeyboardButton createButton(String text, String data) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(data)
                .build();
    }
}
