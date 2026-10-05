package com.example.mpesastore.service;

import com.example.mpesastore.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final List<Product> products = List.of(
            new Product(
                    1L,
                    "Wireless Headphones",
                    "Comfortable Bluetooth headphones with clear sound.",
                    new BigDecimal("2"),
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80"
            ),
            new Product(
                    2L,
                    "Smart Watch",
                    "Modern smartwatch for everyday activity tracking.",
                    new BigDecimal("3"),
                    "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80"
            ),
            new Product(
                    3L,
                    "Backpack",
                    "Durable everyday backpack with laptop compartment.",
                    new BigDecimal("14"),
                    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=80"
            ),
            new Product(
                    4L,
                    "Running Shoes",
                    "Lightweight shoes suitable for daily training.",
                    new BigDecimal("32"),
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80"
            )
    );

    public List<Product> getProducts() {
        return products;
    }

    public Product findById(Long id) {
        return products.stream()
                .filter(product -> product.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
}
