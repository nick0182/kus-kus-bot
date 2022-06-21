package com.shaidulin.kuskusbot.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class URIBuilder {

    public static URI buildURI(String uri, String path, MultiValueMap<String, String> queryParams) {
        return UriComponentsBuilder
                .newInstance()
                .uri(URI.create(uri))
                .path(path)
                .queryParams(queryParams)
                .encode()
                .build()
                .toUri();
    }
}
