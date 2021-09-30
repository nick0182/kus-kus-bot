package com.shaidulin.kuskusbot.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class IngredientValue implements Comparable<IngredientValue> {
    String name;
    int count;

    @Override
    public int compareTo(IngredientValue otherIngredient) {
        return Integer.compare(otherIngredient.count, count);
    }
}
