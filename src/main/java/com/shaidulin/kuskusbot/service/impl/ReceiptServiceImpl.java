package com.shaidulin.kuskusbot.service.impl;

import com.shaidulin.kuskusbot.dto.IngredientMatch;
import com.shaidulin.kuskusbot.service.ReceiptService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final WebClient webClient;

    private final String apiURL;

    @SneakyThrows
    @Override
    public Mono<IngredientMatch> suggestIngredients(String toMatch) {
        return webClient
                .get()
                .uri(apiURL + "/api/vi/ingredients/" + toMatch)
                .retrieve()
                .bodyToMono(IngredientMatch.class);
    }
}
