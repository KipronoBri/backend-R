package com.example.mpesastore.service;

import com.example.mpesastore.model.Order;
import com.example.mpesastore.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final AtomicLong sequence = new AtomicLong(1000);
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();

    public Order createPendingOrder(
            String productName,
            BigDecimal amount,
            String phoneNumber,
            String checkoutRequestId
    ) {
        long id = sequence.incrementAndGet();

        Order order = new Order(
                id,
                productName,
                amount,
                phoneNumber,
                checkoutRequestId,
                PaymentStatus.PENDING,
                LocalDateTime.now()
        );

        orders.put(id, order);
        return order;
    }

    public void updateStatus(String checkoutRequestId, PaymentStatus status) {
        orders.values().stream()
                .filter(order -> checkoutRequestId != null
                        && checkoutRequestId.equals(order.checkoutRequestId()))
                .findFirst()
                .ifPresent(order -> orders.put(
                        order.id(),
                        new Order(
                                order.id(),
                                order.productName(),
                                order.amount(),
                                order.phoneNumber(),
                                order.checkoutRequestId(),
                                status,
                                order.createdAt()
                        )
                ));
    }
}
