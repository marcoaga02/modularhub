package com.marcoaga02.modularhub.core.util;

import com.marcoaga02.modularhub.core.util.constant.PaginationHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseEntityTest {

    @Test
    void fromPage_shouldReturnCorrectHeadersAndBody() {
        List<String> content = List.of("a", "b");
        PageRequest pageable = PageRequest.of(2, 10);
        Page<String> page = new PageImpl<>(content, pageable, 50);

        ResponseEntity<List<String>> response = PageResponseEntity.fromPage(page);

        HttpHeaders headers = response.getHeaders();
        assertThat(headers.getFirst(PaginationHeaders.TOTAL_COUNT)).isEqualTo("50");
        assertThat(headers.getFirst(PaginationHeaders.TOTAL_PAGES)).isEqualTo("5");
        assertThat(headers.getFirst(PaginationHeaders.CURRENT_PAGE)).isEqualTo("2");
        assertThat(headers.getFirst(PaginationHeaders.PAGE_SIZE)).isEqualTo("10");
        assertThat(response.getBody()).containsExactly("a", "b");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void fromPage_shouldReturnEmptyBody_whenPageIsEmpty() {
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        ResponseEntity<List<String>> response = PageResponseEntity.fromPage(page);

        assertThat(response.getBody()).isEmpty();
        assertThat(response.getHeaders().getFirst(PaginationHeaders.TOTAL_COUNT)).isEqualTo("0");
        assertThat(response.getHeaders().getFirst(PaginationHeaders.TOTAL_PAGES)).isEqualTo("0");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

}