package com.shaidulin.kuskusbot.service.util.impl;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.KeyboardProvider;
import com.shaidulin.kuskusbot.service.util.SimpleKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SortPageKeyboardProviderImpl extends KeyboardProvider implements SimpleKeyboardProvider {

    public SortPageKeyboardProviderImpl(StringCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public Mono<InlineKeyboardMarkup> compileKeyboard(String userId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Самые точные", String.valueOf(buttonIndex)));
        Map<Integer, Data.Session> sessionHash = Map.of(buttonIndex, Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_PRESENTATION_INITIAL_PAGE)
                .receiptSortType(SortType.ACCURACY)
                .build());

        return storeSession(userId, sessionHash).thenReturn(InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }
}
