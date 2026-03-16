package com.pitisha.project.mybank.accountservice.api.dto.request;

import com.pitisha.project.mybank.accountservice.domain.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

    @NotNull
    AccountStatus status

) { }
