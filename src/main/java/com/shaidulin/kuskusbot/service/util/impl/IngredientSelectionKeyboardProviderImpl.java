package com.shaidulin.kuskusbot.service.util.impl;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.KeyboardProvider;
import com.shaidulin.kuskusbot.service.util.SimpleKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientSelectionKeyboardProviderImpl extends KeyboardProvider implements SimpleKeyboardProvider {

    public IngredientSelectionKeyboardProviderImpl(StringCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public Mono<InlineKeyboardMarkup> compileKeyboard(String userId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Искать!", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, Data.Session.builder().action(Data.Action.SHOW_SORT_OPTIONS).build());
        buttonIndex++;
        buttons.add(DynamicKeyboard.createButtonRow("Добавить ингредиент", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, Data.Session.builder().action(Data.Action.PROMPT_INGREDIENT).build());

        return storeSession(userId, sessionHash).thenReturn(InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }
}
