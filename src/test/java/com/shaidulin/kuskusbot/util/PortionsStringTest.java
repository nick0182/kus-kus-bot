package com.shaidulin.kuskusbot.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PortionsStringTest {

    private static final String NOT_DEFINED = "Не указано";

    @ParameterizedTest
    @MethodSource("provideSource")
    void test(int portions, String expected) {
        if (portions == 0) {
            assertEquals(expected, NOT_DEFINED);
        } else {
            assertEquals(portions + " " + expected, createPortionsString(portions));
        }
    }

    private static String createPortionsString(int portions) {
        String portionsString = String.valueOf(portions);
        if (portions == 0) {
            return NOT_DEFINED;
        } else if (portions == 1) {
            return portionsString + " порция";
        } else if (portions == 2 || portions == 3 || portions == 4) {
            return portionsString + " порции";
        } else if (portions >= 20 && portions % 10 == 1) {
            return portionsString + " порция";
        } else if (portions >= 20 && (portions % 10 == 2 || portions % 10 == 3 || portions % 10 == 4)) {
            return portionsString + " порции";
        } else {
            return portionsString + " порций";
        }
    }

    private static Stream<Arguments> provideSource() {
        return Stream.of(
                Arguments.of(0, NOT_DEFINED),
                Arguments.of(1, "порция"),
                Arguments.of(2, "порции"),
                Arguments.of(3, "порции"),
                Arguments.of(4, "порции"),
                Arguments.of(5, "порций"),
                Arguments.of(6, "порций"),
                Arguments.of(7, "порций"),
                Arguments.of(8, "порций"),
                Arguments.of(9, "порций"),
                Arguments.of(10, "порций"),
                Arguments.of(11, "порций"),
                Arguments.of(12, "порций"),
                Arguments.of(13, "порций"),
                Arguments.of(14, "порций"),
                Arguments.of(15, "порций"),
                Arguments.of(16, "порций"),
                Arguments.of(17, "порций"),
                Arguments.of(18, "порций"),
                Arguments.of(19, "порций"),
                Arguments.of(20, "порций"),
                Arguments.of(21, "порция"),
                Arguments.of(22, "порции"),
                Arguments.of(23, "порции"),
                Arguments.of(24, "порции"),
                Arguments.of(25, "порций"),
                Arguments.of(26, "порций"),
                Arguments.of(27, "порций"),
                Arguments.of(28, "порций"),
                Arguments.of(29, "порций"),
                Arguments.of(30, "порций"));
    }

}
