package com.shaidulin.kuskusbot.update;

public record Router(Method method, Type type) {

    public enum Method {
        BASE, IMAGE
    }

    public enum Type {
        HOME_PAGE,

        INGREDIENT_SEARCH_PAGE,
        INGREDIENTS_PAGE,
        INGREDIENTS_PAGINATED,
        INGREDIENT_SELECTION,

        RECEIPT_PRESENTATION_PAGE,
        RECEIPT_PRESENTATION_PAGINATED
    }
}