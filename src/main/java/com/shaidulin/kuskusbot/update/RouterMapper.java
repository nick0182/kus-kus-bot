package com.shaidulin.kuskusbot.update;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import static com.shaidulin.kuskusbot.update.Router.Method.BASE;
import static com.shaidulin.kuskusbot.update.Router.Method.IMAGE;
import static com.shaidulin.kuskusbot.update.Router.Type.*;

public record RouterMapper(StringCacheService stringCacheService) {

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
                        .map(ignored -> new Router(BASE, INGREDIENTS_PAGE));
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackMapper.Wrapper callbackWrapper = CallbackMapper.mapCallback(update.getCallbackQuery());
            Router updateKey = identifyCallbackKey(update.getCallbackQuery());
            return stringCacheService
                    .checkPermission(callbackWrapper.userId(), Permission.CALLBACK)
                    .map(ignored -> updateKey);
        }
        return Mono.empty();
    }

    private Router identifyCallbackKey(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        if (callbackData.equals(ButtonConstants.SEARCH_RECEIPTS)) {
            return new Router(IMAGE, RECEIPT_PRESENTATION_PAGE);
        } else if (callbackData.equals(ButtonConstants.START_SEARCH)
                || callbackData.equals(ButtonConstants.SEARCH_NEXT_INGREDIENT)) {
            return new Router(BASE, INGREDIENT_SEARCH_PAGE);
        } else if (callbackData.startsWith(ButtonConstants.INGREDIENTS_PAGE_PAYLOAD_IDENTIFIER)) {
            callbackQuery.setData(resolvePage(callbackData));
            return new Router(BASE, INGREDIENTS_PAGINATED);
        } else if (callbackData.startsWith(ButtonConstants.RECEIPTS_PAGE_PAYLOAD_IDENTIFIER)) {
            callbackQuery.setData(resolvePage(callbackData));
            return new Router(BASE, RECEIPT_PRESENTATION_PAGINATED);
        } else {
            return new Router(BASE, INGREDIENT_SELECTION);
        }
    }

    private String resolvePage(String callbackData) {
        return callbackData.split("_")[1];
    }
}
