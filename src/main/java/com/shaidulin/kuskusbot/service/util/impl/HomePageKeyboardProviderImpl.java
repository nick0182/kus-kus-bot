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

public class HomePageKeyboardProviderImpl extends KeyboardProvider implements SimpleKeyboardProvider {

    public HomePageKeyboardProviderImpl(StringCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public Mono<InlineKeyboardMarkup> compileKeyboard(long userId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Начать поиск", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, Data.Session.builder().action(Data.Action.PROMPT_INGREDIENT).build());
        buttonIndex++;
        buttons.add(DynamicKeyboard.createButtonRow("Инструкция", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, Data.Session.builder().action(Data.Action.SHOW_BOT_REFERENCE).build());
        buttons.add(DynamicKeyboard.createDonationButtonRow());
        return storeSession(userId, sessionHash).thenReturn(InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }
}
