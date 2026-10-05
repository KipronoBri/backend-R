package com.example.mpesastore.controller;

import com.example.mpesastore.dto.StkPushRequest;
import com.example.mpesastore.dto.StkPushResponse;
import com.example.mpesastore.model.PaymentStatus;
import com.example.mpesastore.service.MpesaService;
import com.example.mpesastore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/mpesa")
public class PaymentController {

    private final MpesaService mpesaService;
    private final OrderService orderService;

    public PaymentController(
            MpesaService mpesaService,
            OrderService orderService
    ) {
        this.mpesaService = mpesaService;
        this.orderService = orderService;
    }

    @PostMapping("/stk-push")
    public ResponseEntity<StkPushResponse> stkPush(
            @Valid @RequestBody StkPushRequest request
    ) {
        String phone = normalizeKenyanPhone(request.phoneNumber());

        if (phone == null) {
            return ResponseEntity.badRequest().body(
                    new StkPushResponse(
                            false,
                            "INVALID_PHONE",
                            "Enter a valid Kenyan M-Pesa phone number.",
                            null,
                            null
                    )
            );
        }

        StkPushResponse response = mpesaService.initiateStkPush(
                phone,
                request.amount(),
                request.productName()
        );

        if (response.success() && response.checkoutRequestId() != null) {
            orderService.createPendingOrder(
                    request.productName(),
                    request.amount(),
                    phone,
                    response.checkoutRequestId()
            );
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> callback(
            @RequestBody Map<String, Object> callback
    ) {
        try {
            Map<String, Object> body = castMap(callback.get("Body"));
            Map<String, Object> stkCallback = castMap(body.get("stkCallback"));

            String checkoutRequestId =
                    String.valueOf(stkCallback.get("CheckoutRequestID"));

            int resultCode = Integer.parseInt(
                    String.valueOf(stkCallback.get("ResultCode"))
            );

            orderService.updateStatus(
                    checkoutRequestId,
                    resultCode == 0
                            ? PaymentStatus.PAID
                            : PaymentStatus.FAILED
            );
        } catch (Exception ignored) {
            // Always acknowledge callback. Detailed production logging can be added.
        }

        return ResponseEntity.ok(Map.of("ResultCode", "0"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private String normalizeKenyanPhone(String input) {
        if (input == null) {
            return null;
        }

        String phone = input.replaceAll("\\s+", "")
                .replace("-", "");

        if (phone.startsWith("+254")) {
            phone = phone.substring(1);
        } else if (phone.startsWith("0")) {
            phone = "254" + phone.substring(1);
        }

        if (!phone.matches("254(7|1)\\d{8}")) {
            return null;
        }

        return phone;
    }
}
