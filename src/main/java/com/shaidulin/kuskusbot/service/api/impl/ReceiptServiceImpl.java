package com.shaidulin.kuskusbot.service.api.impl;

import com.shaidulin.kuskusbot.dto.receipt.Page;
import com.shaidulin.kuskusbot.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.util.SortType;
import com.shaidulin.kuskusbot.util.URIBuilder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

public record ReceiptServiceImpl(WebClient webClient, String apiReceiptURL) implements ReceiptService {

    @SneakyThrows
    @Override
    public Mono<IngredientMatch> suggestIngredients(@NonNull String toSearch, List<String> known) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("toSearch", Collections.singletonList(toSearch));
        queryParams.put("known", known);

        return webClient
                .get()
                .uri(URIBuilder.buildURI(apiReceiptURL, "/api/v1/ingredients", queryParams))
                .retrieve()
                .bodyToMono(IngredientMatch.class);
    }

    @Override
    public Mono<ReceiptPresentationMatch> getReceiptPresentations(@NonNull List<String> ingredients,
                                                                  Page page, @NonNull SortType sortType) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("ingredients", ingredients);
        queryParams.put("page.current", Collections.singletonList(String.valueOf(page.current())));
        queryParams.put("page.size", Collections.singletonList(String.valueOf(page.size())));
        queryParams.put("sortType", Collections.singletonList(sortType.name()));

        return webClient
                .get()
                .uri(URIBuilder.buildURI(apiReceiptURL, "/api/v1/receipts/presentations", queryParams))
                .retrieve()
                .bodyToMono(ReceiptPresentationMatch.class);
    }
}
