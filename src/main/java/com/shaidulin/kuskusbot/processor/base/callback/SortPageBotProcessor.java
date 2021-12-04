package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

public record SortPageBotProcessor() implements BaseBotProcessor {

    @Override
    public Mono<? extends BotApiMethod<?>> process(Update update) {
        CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
        return Mono.just(EditMessageReplyMarkup
                .builder()
                .chatId(callbackWrapper.chatId())
                .messageId(callbackWrapper.messageId())
                .replyMarkup(ButtonConstants.sortAccurateChoiceKeyboard)
                .build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.SORT_PAGE;
    }
}
