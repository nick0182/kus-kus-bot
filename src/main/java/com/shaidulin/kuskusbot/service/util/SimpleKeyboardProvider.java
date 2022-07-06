package com.shaidulin.kuskusbot.service.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import reactor.core.publisher.Mono;

public interface SimpleKeyboardProvider {
    Mono<InlineKeyboardMarkup> compileKeyboard(long userId);
}
