package com.shaidulin.kuskusbot.service;

import com.shaidulin.kuskusbot.dto.IngredientMatch;
import reactor.core.publisher.Mono;

public interface ReceiptService {

    Mono<IngredientMatch> suggestIngredients(String toMatch);
}
