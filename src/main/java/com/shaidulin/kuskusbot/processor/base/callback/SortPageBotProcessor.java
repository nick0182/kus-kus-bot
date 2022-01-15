package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Shows receipt sort options
 */
public record SortPageBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageReplyMarkup> process(Data data) {
        return compileSortAccurateChoiceButton(data.getUserId())
                .map(DynamicKeyboard::createSortOptionsChoiceKeyboard)
                .map(keyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .replyMarkup(keyboard)
                        .build());
    }

    private Mono<UUID> compileSortAccurateChoiceButton(String userId) {
        UUID key = UUID.randomUUID();
        Data.Session session = Data.Session
                .builder()
                .action(Data.Action.SHOW_RECEIPT_PRESENTATION_INITIAL_PAGE)
                .receiptSortType(SortType.ACCURACY)
                .build();
        return cacheService.storeSession(userId, key, session).map(ignored -> key);
    }

    @Override
    public Router.Type getType() {
        return Router.Type.SORT_PAGE;
    }
}
