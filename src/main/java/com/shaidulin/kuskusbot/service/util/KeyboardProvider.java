package com.shaidulin.kuskusbot.service.util;

import com.shaidulin.kuskusbot.service.cache.StringCacheService;
import com.shaidulin.kuskusbot.update.Data;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

import static net.logstash.logback.marker.Markers.append;

@AllArgsConstructor
@Slf4j
public abstract class KeyboardProvider {

    private final StringCacheService cacheService;

    protected Mono<Long> storeSession(long userId, Map<Integer, Data.Session> sessionHash) {
        if (log.isTraceEnabled()) {
            log.trace(append("user_id", userId), "Storing session: {}", sessionHash);
        }
        return cacheService.storeSession(userId, sessionHash);
    }
}
