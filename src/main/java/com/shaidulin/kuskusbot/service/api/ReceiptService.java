package com.shaidulin.kuskusbot.service.api;

import com.shaidulin.kuskusbot.dto.IngredientMatch;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReceiptService {

    Mono<IngredientMatch> suggestIngredients(@NonNull String toSearch, @Nullable List<String> known);
}
