package com.shaidulin.kuskusbot.service.cache.impl;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.service.cache.KeyCreator;
import com.shaidulin.kuskusbot.service.cache.ReceiptPresentationCacheService;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

public record ReceiptPresentationCacheServiceImpl(
        RedisReactiveCommands<String, ReceiptPresentationValue> redisReactiveCommands)
        implements ReceiptPresentationCacheService, KeyCreator {

    @Override
    public Mono<Boolean> storeReceiptPresentations(String userId, List<ReceiptPresentationValue> receipts) {
        return redisReactiveCommands
                .rpush(composeKey(userId),
                        receipts.toArray(ReceiptPresentationValue[]::new))
                .map(res -> res != null && res != 0);
    }

    @Override
    public Mono<List<ReceiptPresentationValue>> getReceiptPresentations(String userId) {
        return redisReactiveCommands
                .lrange(composeKey(userId), 0, -1)
                .collectList()
                .defaultIfEmpty(Collections.emptyList());
    }
}
