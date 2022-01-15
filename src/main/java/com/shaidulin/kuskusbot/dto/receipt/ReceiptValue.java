package com.shaidulin.kuskusbot.dto.receipt;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.List;

public record ReceiptValue(@JsonProperty("query-param") int queryParam, String name,
                           @JsonProperty("time-to-cook") Duration cookTime, int portions, List<Ingredient> ingredients,
                           @JsonProperty("nutritional-energy-value") List<Nutrition> nutritions,
                           @JsonProperty("steps-todo") List<Step> steps) {}