package com.shaidulin.kuskusbot.dto.receipt;

import com.shaidulin.kuskusbot.util.SortType;

public record Meta(SortType sortType, int batch, boolean hasMore) {}