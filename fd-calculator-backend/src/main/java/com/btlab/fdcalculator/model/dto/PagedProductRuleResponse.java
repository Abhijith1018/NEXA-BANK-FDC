package com.btlab.fdcalculator.model.dto;

import java.util.List;

public record PagedProductRuleResponse(
    List<ProductRuleDTO> content,
    PageableInfo pageable,
    boolean last,
    int totalElements,
    int totalPages,
    boolean first,
    int size,
    int number,
    SortInfo sort,
    int numberOfElements,
    boolean empty
) {
    public record PageableInfo(
        int pageNumber,
        int pageSize,
        SortInfo sort,
        long offset,
        boolean unpaged,
        boolean paged
    ) {}

    public record SortInfo(
        boolean empty,
        boolean unsorted,
        boolean sorted
    ) {}
}
