package com.shaidulin.kuskusbot.update;

import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class Authorizer {

    private final LettuceCacheService lettuceCacheService;

    public Mono<UpdateKey> authorize(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String userId = message.getFrom().getId().toString();
            if (message.isCommand() && message.getText().equals("/start")) {
                return Mono.just(UpdateKey.HOME_PAGE);
            }
            if (message.isUserMessage() && message.hasText() && !message.getText().equals("")) {
                return lettuceCacheService
                        .checkPermission(userId, Permission.MESSAGE)
                        .map(ignored -> UpdateKey.USER_TEXT);
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            String userId = callbackQuery.getFrom().getId().toString();
            UpdateKey updateKey = identifyCallbackKey(callbackData);
            return lettuceCacheService
                    .checkPermission(userId, Permission.CALLBACK)
                    .map(ignored -> updateKey);
        }
        return Mono.empty();
    }

    private UpdateKey identifyCallbackKey(String callbackData) {
        if (callbackData.equals(ButtonConstants.SEARCH_RECEIPTS)) {
            return UpdateKey.RECEIPTS_PAGE;
        } else if (callbackData.equals(ButtonConstants.START_SEARCH) || callbackData.equals(ButtonConstants.SEARCH_NEXT_INGREDIENT)) {
            return UpdateKey.SEARCH_PAGE;
        } else if (NumberUtils.isCreatable(callbackData)) {
            return UpdateKey.USER_INGREDIENTS_PAGE;
        } else {
            return UpdateKey.USER_INGREDIENT_SELECTION;
        }
    }
}
