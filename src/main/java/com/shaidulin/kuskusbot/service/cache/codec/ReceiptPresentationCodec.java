package com.shaidulin.kuskusbot.service.cache.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import io.lettuce.core.codec.RedisCodec;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record ReceiptPresentationCodec(ObjectMapper objectMapper) implements RedisCodec<String, ReceiptPresentationValue> {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return DEFAULT_CHARSET.decode(bytes).toString();
    }

    @Override
    @SneakyThrows
    public ReceiptPresentationValue decodeValue(ByteBuffer bytes) {
        return objectMapper.readValue(bytes.array(), ReceiptPresentationValue.class);
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return DEFAULT_CHARSET.encode(key);
    }

    @Override
    @SneakyThrows
    public ByteBuffer encodeValue(ReceiptPresentationValue value) {
        return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
    }
}
