package com.pitisha.project.mybank.accountservice.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record ApplicationProperties(

    @NotBlank
    @Pattern(regexp = "^\\d{9}$", message = "Must be 9 digits")
    String bik
) { }
