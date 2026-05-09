package com.relaycore.ingester.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {
    private final String status;
    private final int page;
    private final int limit;
    private final long total;
    private final List<T> data;
}
