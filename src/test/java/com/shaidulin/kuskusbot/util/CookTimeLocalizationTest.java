package com.shaidulin.kuskusbot.util;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.threeten.extra.AmountFormats;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CookTimeLocalizationTest {

    private final Locale russian = new Locale.Builder().setLanguage("ru").setRegion("RU").build();

    @ParameterizedTest(name = "{index} format \"{0}\"")
    @MethodSource("provideSource")
    void test(Duration duration, String expectedFormat) {
        // when
        String formatted = AmountFormats.wordBased(duration, russian);

        // then
        assertEquals(expectedFormat, formatted);
    }

    private Stream<Arguments> provideSource() {
        return Stream.of(
                Arguments.of(Duration.of(90, ChronoUnit.MINUTES), "1 час и 30 минут"),
                Arguments.of(Duration.of(15, ChronoUnit.MINUTES), "15 минут"),
                Arguments.of(Duration.of(2, ChronoUnit.HOURS), "2 часа")
        );
    }
}
