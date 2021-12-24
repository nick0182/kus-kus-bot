package com.shaidulin.kuskusbot.dto.receipt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Portion {
    HUNDRED_GRAMS("100 г блюда", "\uD83D\uDCAF"),
    ALL("Готового блюда", "\uD83E\uDD58"),
    ONE("Порции", "\uD83C\uDF72");

    @Getter
    private final String name;

    @Getter
    private final String emoji;

    @Override
    public String toString() {
        return name;
    }
}
