package com.shaidulin.kuskusbot.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Objects;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class IngredientValue implements Comparable<IngredientValue> {
    String name;
    int count;

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
