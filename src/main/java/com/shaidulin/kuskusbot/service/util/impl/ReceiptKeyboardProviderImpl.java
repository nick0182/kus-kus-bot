package com.shaidulin.kuskusbot.service.util.impl;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.KeyboardProvider;
import com.shaidulin.kuskusbot.service.util.ReceiptKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiptKeyboardProviderImpl extends KeyboardProvider implements ReceiptKeyboardProvider {

    public ReceiptKeyboardProviderImpl(StringCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public Mono<InlineKeyboardMarkup> compileKeyboard(Data data, Data.Action firstButtonAction, String firstButtonText) {
        Data.Session currentSession = data.getSession();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow(firstButtonText, String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, createNewSession(currentSession, firstButtonAction));
        buttonIndex++;
        buttons.add(DynamicKeyboard.createButtonRow("Как готовить", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, createNewSession(currentSession, Data.Action.SHOW_STEP_PAGE));
        buttonIndex++;
        buttons.add(DynamicKeyboard.createButtonRow("↩", String.valueOf(buttonIndex)));
        sessionHash.put(buttonIndex, createNewSession(currentSession, Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE));

        return storeSession(data.getUserId(), sessionHash).thenReturn(InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    private Data.Session createNewSession(Data.Session currentSession, Data.Action action) {
        return Data.Session
                .builder()
                .action(action)
                .receiptSortType(currentSession.getReceiptSortType())
                .receiptId(currentSession.getReceiptId())
                .currentReceiptPage(currentSession.getCurrentReceiptPage())
                .build();
    }
}
