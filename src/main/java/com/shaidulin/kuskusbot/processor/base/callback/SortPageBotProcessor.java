package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.util.SimpleKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import reactor.core.publisher.Mono;

/**
 * Shows receipt sort options
 */
@Slf4j
public record SortPageBotProcessor(SimpleKeyboardProvider keyboardProvider) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageReplyMarkup> process(Data data) {
        return keyboardProvider.compileKeyboard(data.getUserId())
                .map(keyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .replyMarkup(keyboard)
                        .build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.SORT_PAGE;
    }
}
