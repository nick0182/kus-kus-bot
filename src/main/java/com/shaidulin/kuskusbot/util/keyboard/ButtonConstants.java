package com.shaidulin.kuskusbot.util.keyboard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public final class ButtonConstants {

    public static final int INGREDIENTS_PAGE_SIZE = 3;

    public static final String INGREDIENTS_SEARCH_RESULT_MESSAGE = "Вот что смог найти";

    public static final String INGREDIENTS_EMPTY_SEARCH_RESULT_MESSAGE = "Ничего не нашел \uD83E\uDD14 Попробуй еще раз";

    // -------------------- button text ----------------------------

    public static final String SEARCH_RECEIPTS = "Искать!";

    public static final String SEARCH_NEXT_INGREDIENT = "Добавить ингредиент";

    public static final String START_SEARCH = "Начать поиск";

    public static final String SORT_MOST_ACCURATE = "Самые точные";

    public static final String SHOW_INGREDIENTS_OVERVIEW = "Ингредиенты";

    public static final String SHOW_NUTRITION_OVERVIEW = "Энергетическая ценность";

    public static final String SHOW_STEPS = "Как готовить";

    public static final String RETURN_TO_RECEIPTS = "↩";
}
