package com.shaidulin.kuskusbot.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.TreeSet;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class IngredientMatch {
    TreeSet<IngredientValue> ingredients;
}
