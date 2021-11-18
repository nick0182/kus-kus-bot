package com.shaidulin.kuskusbot.dto.receipt;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

public record ReceiptPresentationValue(@JsonProperty("query-param") int queryParam, String name,
                                       @JsonProperty("time-to-cook") Duration cookTime, int portions) {}