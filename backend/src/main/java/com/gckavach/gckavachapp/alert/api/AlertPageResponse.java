package com.gckavach.gckavachapp.alert.api;

import org.springframework.data.domain.Page;

import java.util.List;

public record AlertPageResponse(
        List<AlertResponse> content,
                                int page,
                                int size,
                                long totalElements,
                                int totalPages,
                                boolean first,
                                boolean last
) {
    public static AlertPageResponse from(Page<AlertResponse> page) {
        return new AlertPageResponse(page.getContent(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}