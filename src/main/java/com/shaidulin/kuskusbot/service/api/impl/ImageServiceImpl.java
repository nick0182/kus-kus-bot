package com.shaidulin.kuskusbot.service.api.impl;

import com.shaidulin.kuskusbot.service.api.ImageService;
import com.shaidulin.kuskusbot.util.ImageType;
import com.shaidulin.kuskusbot.util.URIBuilder;
import lombok.SneakyThrows;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

public record ImageServiceImpl(WebClient webClient, String apiImageURL) implements ImageService {

    @SneakyThrows
    @Override
    public Mono<byte[]> fetchImage(@NonNull ImageType type, @NonNull String id) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("type", Collections.singletonList(type.name()));
        queryParams.put("id", Collections.singletonList(id));

        return webClient
                .get()
                .uri(URIBuilder.buildURI(apiImageURL, "/api/v1/image", queryParams))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }
}
