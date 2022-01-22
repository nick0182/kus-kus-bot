package com.shaidulin.kuskusbot.processor.base.text;

import com.shaidulin.kuskusbot.dto.ingredient.IngredientValue;
import com.shaidulin.kuskusbot.processor.base.BaseBotProcessor;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import com.shaidulin.kuskusbot.update.Router;
import com.shaidulin.kuskusbot.util.keyboard.DynamicKeyboard;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Shows the first page's offering of 3 possible ingredients
 */
public record IngredientsPageBotProcessor(StringCacheService cacheService, ReceiptService receiptService)
        implements BaseBotProcessor {

    @Override
    public Mono<SendMessage> process(Data data) {
        String userId = data.getUserId();
        String toSearch = data.getInput();
        return cacheService
                .getIngredients(userId)
                .flatMap(known -> receiptService.suggestIngredients(toSearch, known))
                .map(ingredientMatch -> {
                    if (ingredientMatch.ingredients().isEmpty()) {
                        throw new IllegalArgumentException(); // FIXME create custom exception
                    } else {
                        return ingredientMatch.ingredients();
                    }
                })
                .filterWhen(ingredientMatch -> cacheService.storeIngredientSuggestions(userId, ingredientMatch))
                .flatMap(ingredients -> compileIngredientButtons(userId, ingredients))
                .map(ingredientsKeyboard -> SendMessage
                        .builder()
                        .text("Вот что смог найти")
                        .chatId(userId)
                        .replyMarkup(ingredientsKeyboard)
                        .build()
                )
                .onErrorReturn(IllegalArgumentException.class,
                        SendMessage
                                .builder()
                                .text("Ничего не нашел \uD83E\uDD14 Попробуй еще раз")
                                .chatId(userId)
                                .build()
                );
    }

    private Mono<InlineKeyboardMarkup> compileIngredientButtons(String userId, TreeSet<IngredientValue> ingredients) {
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

        if (!CollectionUtils.isEmpty(ingredients)) {
            buttons.add(DynamicKeyboard.createNavigationPanelRow(null, buttonCurrentIndex));
            sessionHash.put(buttonCurrentIndex, Data.Session
                    .builder()
                    .action(Data.Action.SHOW_INGREDIENTS_PAGE)
                    .currentIngredientsPage(1)
                    .build());
        }

        return cacheService
                .storeSession(userId, sessionHash)
                .map(ignored -> InlineKeyboardMarkup.builder().keyboard(buttons).build());
    }

    @Override
    public Router.Type getType() {
        return Router.Type.INGREDIENTS_PAGE;
    }
}
