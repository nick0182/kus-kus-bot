package com.shaidulin.kuskusbot.dto.ingredient;

import java.util.Objects;

public record IngredientValue(String name, int count) implements Comparable<IngredientValue> {

    @Override
    public int compareTo(IngredientValue otherIngredient) {
        int countCompared = Integer.compare(otherIngredient.count, count);
        if (countCompared == 0) {
            return Objects.requireNonNull(name).compareTo(Objects.requireNonNull(otherIngredient.name));
        } else {
            return countCompared;
        }
    }
}
