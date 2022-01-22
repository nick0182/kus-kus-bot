package com.shaidulin.kuskusbot.processor.base.callback;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Shows a paginated offering of 3 possible ingredients
 */
public record IngredientsPaginatedBotProcessor(StringCacheService cacheService) implements BaseBotProcessor {

    private static final int INGREDIENTS_PAGE_SIZE = 3;

    @Override
    public Mono<EditMessageReplyMarkup> process(Data data) {
        int page = data.getSession().getCurrentIngredientsPage();
        return cacheService
                .getIngredientSuggestions(data.getUserId(), page * INGREDIENTS_PAGE_SIZE)
                .flatMap(ingredients -> compileIngredientButtons(data.getUserId(), page, ingredients))
                .map(ingredientsKeyboard -> EditMessageReplyMarkup
                        .builder()
                        .chatId(data.getChatId())
                        .messageId(data.getMessageId())
                        .replyMarkup(ingredientsKeyboard)
                        .build());
    }

    private Mono<InlineKeyboardMarkup> compileIngredientButtons(String userId, int page, TreeSet<IngredientValue> ingredients) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Map<Integer, Data.Session> sessionHash = new HashMap<>();
        int buttonCurrentIndex = -1;

        IngredientValue ingredient;
        while ((ingredient = ingredients.pollFirst()) != null && ++buttonCurrentIndex < 3) {
            String name = ingredient.name();
            int count = ingredient.count();
            String text = String.join(" - ", name, String.valueOf(count));
            buttons.add(DynamicKeyboard.createButtonRow(text, String.valueOf(buttonCurrentIndex)));
            sessionHash.put(buttonCurrentIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_SEARCH_CONFIGURATION_OPTIONS)
                    .ingredientName(name)
                    .build());
        }

        Integer previousPageButtonIndex = null;
        Integer nextPageButtonIndex = null;

        if (page != 0) {
            previousPageButtonIndex = buttonCurrentIndex;
            sessionHash.put(previousPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(page - 1)
                    .build());
        }

        if (!CollectionUtils.isEmpty(ingredients)) {
            nextPageButtonIndex = ++buttonCurrentIndex;
            sessionHash.put(nextPageButtonIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(page + 1)
                    .build());
        }

        buttons.add(DynamicKeyboard.createNavigationPanelRow(previousPageButtonIndex, nextPageButtonIndex));

        return cacheService
                .storeSession(userId, sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENTS_PAGINATED;
    }
}
