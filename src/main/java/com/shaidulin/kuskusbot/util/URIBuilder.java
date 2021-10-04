package com.shaidulin.kuskusbot.util;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

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
