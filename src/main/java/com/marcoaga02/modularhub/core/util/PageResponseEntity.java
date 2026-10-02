package com.marcoaga02.modularhub.core.util;

import com.marcoaga02.modularhub.core.util.constant.PaginationHeaders;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class PageResponseEntity {

    private PageResponseEntity() {}

    public static <T> ResponseEntity<List<T>> fromPage(Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(PaginationHeaders.TOTAL_COUNT, String.valueOf(page.getTotalElements()));
        headers.add(PaginationHeaders.TOTAL_PAGES, String.valueOf(page.getTotalPages()));
        headers.add(PaginationHeaders.CURRENT_PAGE, String.valueOf(page.getNumber()));
        headers.add(PaginationHeaders.PAGE_SIZE, String.valueOf(page.getSize()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(page.getContent());
    }
}
