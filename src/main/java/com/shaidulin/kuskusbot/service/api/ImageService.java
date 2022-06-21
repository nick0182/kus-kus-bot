package com.shaidulin.kuskusbot.service.api;

import com.shaidulin.kuskusbot.util.ImageType;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface ImageService {

    Mono<byte[]> fetchImage(@NonNull ImageType type, @NonNull String id);
}
