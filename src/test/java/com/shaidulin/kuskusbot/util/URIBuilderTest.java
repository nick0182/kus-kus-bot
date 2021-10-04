package com.shaidulin.kuskusbot.util;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class URIBuilderTest {

    @Test
    void test() {
        // given
        String uri = "http://localhost:9658";
        String path = "/api/vi/ingredients";
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put("toSearch", Collections.singletonList("сыр"));

        // when
        URI expected = UriComponentsBuilder
                .newInstance()
                .uri(URI.create("http://localhost:9658/api/vi/ingredients?toSearch=сыр"))
                .encode()
                .build()
                .toUri();
        URI actual = URIBuilder.buildURI(uri, path, queryParams);

        // then
        assertEquals(expected, actual);

        // when
        queryParams.put("known", Collections.emptyList());
        expected = UriComponentsBuilder
                .newInstance()
                .uri(URI.create("http://localhost:9658/api/vi/ingredients?toSearch=сыр&known"))
                .encode()
                .build()
                .toUri();
        actual = URIBuilder.buildURI(uri, path, queryParams);

        // then
        assertEquals(expected, actual);
    }
}