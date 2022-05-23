package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.util.SimpleKeyboardProvider;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import reactor.core.publisher.Mono;

public record BotReferencePageBotProcessor(SimpleKeyboardProvider keyboardProvider,
                                           String manualText) implements BaseBotProcessor {

    @Override
    public Mono<EditMessageText> process(Data data) {
        return keyboardProvider.compileKeyboard(data.getUserId())
                .map(keyboardProvider -> EditMessageText
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .text(manualText)
                        .replyMarkup(keyboardProvider)
                        .parseMode("HTML")
                        .build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.BOT_REFERENCE_PAGE;
    }
}
