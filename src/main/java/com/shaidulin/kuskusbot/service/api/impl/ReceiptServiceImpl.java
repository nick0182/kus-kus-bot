package com.shaidulin.kuskusbot.service.api.impl;

import com.shaidulin.kuskusbot.dto.IngredientMatch;
import com.shaidulin.kuskusbot.service.api.ReceiptService;
import com.shaidulin.kuskusbot.util.URIBuilder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final WebClient webClient;

    private final String apiURL;

    @SneakyThrows
    @Override
    public Mono<IngredientMatch> suggestIngredients(@NonNull String toSearch, List<String> known) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("toSearch", Collections.singletonList(toSearch));
        queryParams.put("known", known);

        return webClient
                .get()
                .uri(URIBuilder.buildURI(apiURL, "/api/v1/ingredients", queryParams))
                .retrieve()
                .bodyToMono(IngredientMatch.class);
    }
}
