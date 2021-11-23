package com.shaidulin.kuskusbot.service.cache;

public interface KeyCreator {

    default String composeKey(String userId, String suffix) {
        return String.join(":", userId, suffix);
    }

    default String composeKey(String userId) {
        return String.join(":", userId, "receipts", "presentations");
    }

    default String composeMainImageKey(String id) {
        return String.join(":", "image", id);
    }
}
