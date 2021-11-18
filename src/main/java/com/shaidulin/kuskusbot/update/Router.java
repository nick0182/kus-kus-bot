package com.shaidulin.kuskusbot.update;

public record Router(Method method, Type type) {

    public enum Method {
        BASE, IMAGE
    }

    public enum Type {
        HOME_PAGE,
        SEARCH_PAGE,
        USER_TEXT,
        USER_INGREDIENTS_PAGE,
        USER_INGREDIENT_SELECTION,
        RECEIPTS_PAGE
    }
}