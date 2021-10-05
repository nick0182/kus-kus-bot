package com.shaidulin.kuskusbot.update;

import com.shaidulin.kuskusbot.service.cache.LettuceCacheService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class Authorizer {

    private static final String HOME_PAGE_COMMAND = "/start";

    private static final String SEARCH_PAGE_COMMAND = "/search";

    private final LettuceCacheService lettuceCacheService;

    public Mono<UpdateKey> authorize(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String userId = message.getFrom().getId().toString();
            if (message.isCommand()) {
                if (message.getText().equals(HOME_PAGE_COMMAND)) {
                    return Mono.just(UpdateKey.HOME_PAGE);
                }
                if (message.getText().equals(SEARCH_PAGE_COMMAND)) {
                    return lettuceCacheService
                            .checkPermission(userId, Permission.COMMAND)
                            .map(ignored -> UpdateKey.SEARCH_PAGE);
                }
            }
            if (message.isUserMessage() && message.hasText() && !message.getText().equals("")) {
                return lettuceCacheService
                        .checkPermission(userId, Permission.MESSAGE)
                        .map(ignored -> UpdateKey.USER_TEXT);
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String buttonData = callbackQuery.getData();
            String userId = callbackQuery.getFrom().getId().toString();
            UpdateKey updateKey = NumberUtils.isCreatable(buttonData) ? UpdateKey.USER_INGREDIENTS_PAGE : UpdateKey.USER_INGREDIENT_SELECTION;
            return lettuceCacheService
                    .checkPermission(userId, Permission.CALLBACK)
                    .map(ignored -> updateKey);
        }
        return Mono.empty();

    }
}
