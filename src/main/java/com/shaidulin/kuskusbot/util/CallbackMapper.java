package com.shaidulin.kuskusbot.util;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public final class CallbackMapper {

    private CallbackMapper() {}

    public static Wrapper mapCallback(CallbackQuery query) {
        String userId = query.getFrom().getId().toString();
        Message message = query.getMessage();
        String chatId = message.getChatId().toString();
        Integer messageId = message.getMessageId();
        String data = query.getData();
        return new Wrapper(userId, chatId, messageId, data);
    }

    public record Wrapper(String userId, String chatId, Integer messageId, String data) {}
}
