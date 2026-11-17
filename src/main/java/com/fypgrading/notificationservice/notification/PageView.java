package com.fypgrading.notificationservice.notification;

import java.util.List;
import org.springframework.data.domain.Page;

/** The contract's paged list: {@code {content, page: {size, number, totalElements, totalPages}}}. */
public record PageView<T>(List<T> content, PageMeta page) {

    public static <T> PageView<T> of(Page<T> page) {
        return new PageView<>(
                page.getContent(),
                new PageMeta(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages()));
    }

    public record PageMeta(int size, int number, long totalElements, int totalPages) {}
}
