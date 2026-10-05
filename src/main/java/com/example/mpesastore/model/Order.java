package com.example.mpesastore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Order(
        Long id,
        String productName,
        BigDecimal amount,
        String phoneNumber,
        String checkoutRequestId,
        PaymentStatus status,
        LocalDateTime createdAt
) {}
