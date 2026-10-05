package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public record TokenPackageResponse(
        Long id,
        String name,
        BigDecimal price,
        int tokens,
        List<String> features,
        String badge
) {
}