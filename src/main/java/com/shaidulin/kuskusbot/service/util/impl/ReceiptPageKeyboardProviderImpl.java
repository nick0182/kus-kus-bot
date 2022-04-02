package com.shaidulin.kuskusbot.service.util.impl;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.KeyboardProvider;
import com.shaidulin.kuskusbot.service.util.ReceiptPageKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

public class ReceiptPageKeyboardProviderImpl extends KeyboardProvider implements ReceiptPageKeyboardProvider {

    public ReceiptPageKeyboardProviderImpl(StringCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public Mono<InlineKeyboardMarkup> compileKeyboard(Data data, int receiptId, boolean hasMoreReceipts) {
        Data.Session currentSession = data.getSession();
        int currentReceiptPage = Optional.ofNullable(currentSession.getCurrentReceiptPage()).orElse(0);
        SortType receiptSortType = currentSession.getReceiptSortType();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonCurrentIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Ингредиенты", String.valueOf(buttonCurrentIndex)));
        sessionHash.put(buttonCurrentIndex, Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_INGREDIENTS_PAGE)
                .receiptSortType(receiptSortType)
                .receiptId(receiptId)
                .currentReceiptPage(currentReceiptPage)
                .build());

        Integer previousPageButtonIndex = null;
        Integer nextPageButtonIndex = null;

        if (currentReceiptPage > 0) {
            previousPageButtonIndex = ++buttonCurrentIndex;
            sessionHash.put(previousPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                    .receiptSortType(receiptSortType)
                    .currentReceiptPage(currentReceiptPage - 1)
                    .build());
        }

        if (hasMoreReceipts) {
            nextPageButtonIndex = ++buttonCurrentIndex;
            sessionHash.put(nextPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                    .receiptSortType(receiptSortType)
                    .currentReceiptPage(currentReceiptPage + 1)
                    .build());
        }

        buttons.add(DynamicKeyboard.createNavigationPanelRow(previousPageButtonIndex, nextPageButtonIndex));

        return storeSession(data.getUserId(), sessionHash).thenReturn(InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }
}
