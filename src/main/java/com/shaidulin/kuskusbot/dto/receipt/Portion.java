package com.shaidulin.kuskusbot.dto.receipt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Portion {
    HUNDRED_GRAMS("100 г блюда"),
    ALL("Готового блюда"),
    ONE("Порции");

    @Getter
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
