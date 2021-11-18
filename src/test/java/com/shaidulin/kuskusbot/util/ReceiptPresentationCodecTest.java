package com.shaidulin.kuskusbot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shaidulin.kuskusbot.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskusbot.service.cache.codec.ReceiptPresentationCodec;
import io.lettuce.core.codec.RedisCodec;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptPresentationCodecTest {

    @Test
    void testReceiptPresentationCodec() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        RedisCodec<String, ReceiptPresentationValue> codec = new ReceiptPresentationCodec(objectMapper);

        ReceiptPresentationValue value = new ReceiptPresentationValue(12, "Котлеты", Duration.ofMinutes(20), 5);

        // when
        ReceiptPresentationValue result = codec.decodeValue(codec.encodeValue(value));

        // then
        assertEquals(value, result);
    }
}