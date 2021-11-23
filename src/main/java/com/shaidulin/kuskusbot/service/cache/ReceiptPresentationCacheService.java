package com.shaidulin.kuskusbot.service.cache;

import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReceiptPresentationCacheService {

    Mono<Boolean> storeReceiptPresentations(String userId, List<ReceiptPresentationValue> receipts);

    Mono<List<ReceiptPresentationValue>> getReceiptPresentations(String userId);
}
