package com.shaidulin.kuskusbot.update;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.util.ButtonConstants;
import com.shaidulin.kuskusbot.util.CallbackMapper;
import com.shaidulin.kuskusbot.util.SortType;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static com.shaidulin.kuskusbot.update.Router.Method.*;
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
            return new Router(BASE, SORT_PAGE);
        } else if (callbackData.equals(ButtonConstants.START_SEARCH)
                || callbackData.equals(ButtonConstants.SEARCH_NEXT_INGREDIENT)) {
            return new Router(BASE, INGREDIENT_SEARCH_PAGE);
        } else if (callbackData.startsWith(ButtonConstants.RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX)) {
            callbackQuery.setData(resolveReceiptPage(callbackData));
            return new Router(BASE, RECEIPT_WITH_INGREDIENTS_PAGE);
        } else if (callbackData.startsWith(ButtonConstants.INGREDIENTS_PAGE_PAYLOAD_PREFIX)) {
            callbackQuery.setData(resolvePage(callbackData));
            return new Router(BASE, INGREDIENTS_PAGINATED);
        } else if (callbackData.startsWith(ButtonConstants.RECEIPTS_PAGE_PAYLOAD_PREFIX)) {
            callbackQuery.setData(resolvePage(callbackData));
            return new Router(IMAGE_EDIT, RECEIPT_PRESENTATION_PAGINATED);
        } else if (Arrays.stream(SortType.values()).anyMatch(sortType -> sortType.name().equals(callbackData))) {
            return new Router(IMAGE_SEND, RECEIPT_PRESENTATION_PAGE);
        } else {
            return new Router(BASE, INGREDIENT_SELECTION);
        }
    }

    private String resolvePage(String callbackData) {
        return callbackData.split("_")[1];
    }

    private String resolveReceiptPage(String callbackData) {
        return callbackData.replace(ButtonConstants.RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX + "_", "");
    }
}
