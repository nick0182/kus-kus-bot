package com.shaidulin.kuskusbot.service.util.impl;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.service.util.KeyboardProvider;
import com.shaidulin.kuskusbot.service.util.StepPageKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

public class StepPageKeyboardProviderImpl extends KeyboardProvider implements StepPageKeyboardProvider {

    public StepPageKeyboardProviderImpl(StringCacheService cacheService) {
        super(cacheService);
    }

    @Override
    public Mono<InlineKeyboardMarkup> compileKeyboard(Data data, boolean hasMoreReceipts) {
        Data.Session currentSession = data.getSession();
        int currentReceiptPage = currentSession.getCurrentReceiptPage();
        int currentStepPage = Optional.ofNullable(currentSession.getCurrentStepPage()).orElse(0);
        int receiptId = currentSession.getReceiptId();
        SortType receiptSortType = currentSession.getReceiptSortType();

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonCurrentIndex = 0;

        Integer previousPageButtonIndex = null;
        Integer nextPageButtonIndex = null;

        if (currentStepPage > 0) {
            previousPageButtonIndex = buttonCurrentIndex++;
            sessionHash.put(previousPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_STEP_PAGE)
                    .receiptId(receiptId)
                    .currentReceiptPage(currentReceiptPage)
                    .receiptSortType(receiptSortType)
                    .currentStepPage(currentStepPage - 1)
                    .build());
        }

        if (hasMoreReceipts) {
            nextPageButtonIndex = buttonCurrentIndex++;
            sessionHash.put(nextPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_STEP_PAGE)
                    .receiptId(receiptId)
                    .currentReceiptPage(currentReceiptPage)
                    .receiptSortType(receiptSortType)
                    .currentStepPage(currentStepPage + 1)
                    .build());
        }

        buttons.add(DynamicKeyboard.createNavigationPanelRow(previousPageButtonIndex, nextPageButtonIndex));

        buttons.add(DynamicKeyboard.createButtonRow("К рецепту", String.valueOf(buttonCurrentIndex)));
        sessionHash.put(buttonCurrentIndex, Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_PRESENTATION_PAGE)
                .receiptId(receiptId)
                .currentReceiptPage(currentReceiptPage)
                .receiptSortType(receiptSortType)
                .build());

        return storeSession(data.getUserId(), sessionHash).thenReturn(InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }
}
