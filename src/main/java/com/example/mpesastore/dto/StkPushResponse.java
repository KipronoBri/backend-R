package com.example.mpesastore.dto;

public record StkPushResponse(
        boolean success,
        String responseCode,
        String responseDescription,
        String merchantRequestId,
        String checkoutRequestId
) {}
