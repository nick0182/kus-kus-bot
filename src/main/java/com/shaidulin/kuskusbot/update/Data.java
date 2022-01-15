package com.shaidulin.kuskusbot.update;

import com.shaidulin.kuskusbot.util.SortType;
import lombok.*;

@Value
@Builder
public class Data {
    String userId;
    String firstName;
    String lastName;
    String chatId;
    Integer messageId;
    String input;
    Session session;

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @Builder
    public static class Session {
        Action action;
        String ingredientName;
        Integer currentIngredientsPage;
        Integer receiptId;
        Integer currentReceiptPage;
        SortType receiptSortType;
        Integer currentStepPage;
    }

    public enum Action {
        PROMPT_INGREDIENT, // user wants to prompt for ingredient
        SHOW_INGREDIENTS_PAGE,
        SHOW_SEARCH_CONFIGURATION_OPTIONS,
        SHOW_SORT_OPTIONS,
        SHOW_RECEIPT_PRESENTATION_INITIAL_PAGE, // first page after search triggered
        SHOW_RECEIPT_PRESENTATION_PAGE,
        SHOW_RECEIPT_INGREDIENTS_PAGE,
        SHOW_RECEIPT_NUTRITION_PAGE,
        SHOW_STEP_PAGE
    }
}
