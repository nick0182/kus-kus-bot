package com.shaidulin.kuskusbot.update;

public record Router(Method method, Type type) {

    public enum Method {
        BASE, IMAGE_SEND, IMAGE_EDIT
    }

    public enum Type {
        HOME_PAGE,

        INGREDIENT_SEARCH_PAGE,
        INGREDIENTS_PAGE,
        INGREDIENTS_PAGINATED,
        INGREDIENT_SELECTION,

        SORT_PAGE,

        RECEIPT_PRESENTATION_PAGE,
        RECEIPT_PRESENTATION_PAGINATED,
        RECEIPT_WITH_INGREDIENTS_PAGE,
        RECEIPT_WITH_NUTRITION_OVERVIEW_PAGE
    }
}