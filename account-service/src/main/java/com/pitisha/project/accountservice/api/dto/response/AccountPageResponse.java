package com.pitisha.project.accountservice.api.dto.response;

import java.util.List;

public record AccountPageResponse(
        List<AccountResponse> data,
        Integer totalPages,
        Integer currentPage,
        Integer pageSize
) {}
