package com.shaidulin.kuskusbot.update;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import static com.shaidulin.kuskusbot.update.Router.Method.*;
import static com.shaidulin.kuskusbot.update.Router.Type.*;

@AllArgsConstructor
public class RouterMapper {

    private final StringCacheService stringCacheService;

    public Mono<Router> routeIncomingUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String userId = message.getFrom().getId().toString();
            if (message.isCommand() && message.getText().equals("/start")) {
                return Mono.just(new Router(BASE, HOME_PAGE));
            }
            if (message.isUserMessage() && message.hasText() && !message.getText().equals("")) {
                return stringCacheService
                        .checkPermission(userId, Permission.MESSAGE)
                        .map(ignored -> new Router(BASE, USER_TEXT));
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
            Router updateKey = identifyCallbackKey(callbackWrapper.data());
            return stringCacheService
                    .checkPermission(callbackWrapper.userId(), Permission.CALLBACK)
                    .map(ignored -> updateKey);
        }
        return Mono.empty();
    }

    private Router identifyCallbackKey(String callbackData) {
        if (callbackData.equals(ButtonConstants.SEARCH_RECEIPTS)) {
            return new Router(IMAGE, RECEIPTS_PAGE);
        } else if (callbackData.equals(ButtonConstants.START_SEARCH) || callbackData.equals(ButtonConstants.SEARCH_NEXT_INGREDIENT)) {
            return new Router(BASE, SEARCH_PAGE);
        } else if (NumberUtils.isCreatable(callbackData)) {
            return new Router(BASE, USER_INGREDIENTS_PAGE);
        } else {
            return new Router(BASE, USER_INGREDIENT_SELECTION);
        }
    }
}
