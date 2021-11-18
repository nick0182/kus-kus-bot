package com.shaidulin.kuskusbot.dto.ingredient;

import java.util.TreeSet;

public record IngredientMatch(TreeSet<IngredientValue> ingredients) {}