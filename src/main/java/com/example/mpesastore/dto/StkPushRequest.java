package com.example.mpesastore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record StkPushRequest(
        @NotBlank String phoneNumber,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String productName
) {}
