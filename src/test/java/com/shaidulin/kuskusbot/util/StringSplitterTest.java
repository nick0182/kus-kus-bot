package com.shaidulin.kuskusbot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringSplitterTest {

    @Test
    void test() {
        // given
        String toSplit = String.join("_", ButtonConstants.RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX, "15", "184");
        String expected = String.join("_", "15", "184");

        // expect
        assertEquals(expected, toSplit.replace(ButtonConstants.RECEIPT_WITH_INGREDIENTS_PAGE_PAYLOAD_PREFIX + "_", ""));
    }
}
