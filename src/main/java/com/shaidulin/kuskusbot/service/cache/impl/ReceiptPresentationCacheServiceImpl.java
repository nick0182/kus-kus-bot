package com.shaidulin.kuskusbot.service.cache.impl;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.service.cache.ReceiptPresentationCacheService;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import reactor.core.publisher.Mono;

import java.util.List;

public record ReceiptPresentationCacheServiceImpl(
        RedisReactiveCommands<String, ReceiptPresentationValue> redisReactiveCommands)
        implements ReceiptPresentationCacheService {

    @Override
    public Mono<Boolean> storeReceiptPresentations(String userId, List<ReceiptPresentationValue> receipts) {
        return redisReactiveCommands
                .multi()
                .doOnSuccess(ignored ->
                        redisReactiveCommands
                                .rpush(composeKey(userId, "receipts", "presentations"),
                                        receipts.toArray(ReceiptPresentationValue[]::new))
                                .subscribe())
                .then(redisReactiveCommands.exec())
                .map(objects -> !objects.wasDiscarded())
                .filter(Boolean.TRUE::equals);
    }

    private String composeKey(String userId, String suffix1, String suffix2) {
        return String.join(":", userId, suffix1, suffix2);
    }
}
