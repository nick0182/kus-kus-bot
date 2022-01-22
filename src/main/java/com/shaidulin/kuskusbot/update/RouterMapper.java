package com.shaidulin.kuskusbot.update;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;

import static com.shaidulin.kuskusbot.update.Router.Method.*;
import static com.shaidulin.kuskusbot.update.Router.Type.*;

public record RouterMapper(StringCacheService stringCacheService) {

    public Mono<Router> routeIncomingUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            User from = message.getFrom();
            String userId = from.getId().toString();
            if (message.isCommand() && message.getText().equals("/start")) {
                return Mono.just(new Router(BASE, HOME_PAGE, constructDataFromMessage(message)));
            }
            if (message.isUserMessage() && message.hasText() && !message.getText().equals("")) {
                return stringCacheService
                        .checkPermission(userId, Permission.MESSAGE)
                        .map(ignored -> new Router(BASE, INGREDIENTS_PAGE, constructDataFromMessage(message)));
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String userId = callbackQuery.getFrom().getId().toString();
            String sessionId = callbackQuery.getData();
            return stringCacheService
                    .checkPermission(userId, Permission.CALLBACK)
                    .flatMap(ignored -> stringCacheService.getSession(userId, sessionId))
                    .map(session -> constructDataFromCallback(callbackQuery, session))
                    .map(this::identifyCallbackRouter);
        }
        return Mono.empty();
    }

    private Data constructDataFromMessage(Message message) {
        User from = message.getFrom();
        return Data.builder()
                .userId(from.getId().toString())
                .firstName(from.getFirstName())
                .lastName(from.getLastName())
                .chatId(message.getChatId().toString())
                .messageId(message.getMessageId())
                .input(message.getText())
                .build();
    }

    private Data constructDataFromCallback(CallbackQuery query, Data.Session session) {
        User from = query.getFrom();
        return Data.builder()
                .userId(from.getId().toString())
                .firstName(from.getFirstName())
                .lastName(from.getLastName())
                .chatId(query.getMessage().getChatId().toString())
                .messageId(query.getMessage().getMessageId())
                .session(session)
                .build();
    }

    private Router identifyCallbackRouter(Data data) {
        return switch (data.getSession().getAction()) {
            case PROMPT_INGREDIENT -> new Router(BASE, INGREDIENT_SEARCH_PAGE, data);
            case SHOW_INGREDIENTS_PAGE -> new Router(BASE, INGREDIENTS_PAGINATED, data);
            case SHOW_SEARCH_CONFIGURATION_OPTIONS -> new Router(BASE, INGREDIENT_SELECTION, data);
            case SHOW_SORT_OPTIONS -> new Router(BASE, SORT_PAGE, data);
            case SHOW_RECEIPT_PRESENTATION_INITIAL_PAGE -> new Router(IMAGE_SEND, RECEIPT_PRESENTATION_PAGE, data);
            case SHOW_RECEIPT_PRESENTATION_PAGE -> new Router(IMAGE_EDIT, RECEIPT_PRESENTATION_PAGINATED, data);
            case SHOW_RECEIPT_INGREDIENTS_PAGE -> new Router(BASE, RECEIPT_WITH_INGREDIENTS_PAGE, data);
            case SHOW_RECEIPT_NUTRITION_PAGE -> new Router(BASE, RECEIPT_WITH_NUTRITION_OVERVIEW_PAGE, data);
            case SHOW_STEP_PAGE -> new Router(BASE, RECEIPT_WITH_NUTRITION_OVERVIEW_PAGE, data);
        };
    }
}
