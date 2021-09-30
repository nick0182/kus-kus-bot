package com.shaidulin.kuskusbot.update;

import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateKeyMapper {

    private static final String HOME_PAGE_COMMAND = "/start";

    private static final String SEARCH_PAGE_COMMAND = "/search";

    public static UpdateKey mapKey(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.isCommand()) {
                if (message.getText().equals(HOME_PAGE_COMMAND)) {
                    return UpdateKey.HOME_PAGE;
                }
                if (message.getText().equals(SEARCH_PAGE_COMMAND)) {
                    return UpdateKey.SEARCH_PAGE;
                }
            }
            if (message.isUserMessage() && message.hasText() && !message.getText().equals("")) {
                return UpdateKey.USER_TEXT;
            }
        }
        if (update.hasCallbackQuery()) {
            String buttonData = update.getCallbackQuery().getData();
            if (NumberUtils.isCreatable(buttonData)) {
                return UpdateKey.USER_INGREDIENTS_PAGE;
            } else {
                return UpdateKey.USER_INGREDIENT_SELECTION;
            }
        }
        return null;

    }
}
