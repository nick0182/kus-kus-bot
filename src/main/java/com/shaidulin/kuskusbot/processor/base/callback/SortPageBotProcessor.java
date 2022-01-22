package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows receipt sort options
 */
public record SortPageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageReplyMarkup> process(Data data) {
        return compileSortAccurateChoiceButton(data.getUserId())
                .map(keyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .replyMarkup(keyboard)
                        .build());
    }

    private Mono<InlineKeyboardMarkup> compileSortAccurateChoiceButton(String userId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        int buttonIndex = 0;
        buttons.add(DynamicKeyboard.createButtonRow("Самые точные", String.valueOf(buttonIndex)));
        return cacheService
                .storeSession(userId, Map.of(buttonIndex, Data.Session
                        .builder()
                        .action(Data.Action.SHOW_RECEIPT_PRESENTATION_INITIAL_PAGE)
                        .receiptSortType(SortType.ACCURACY)
                        .build()))
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.SORT_PAGE;
    }
}
