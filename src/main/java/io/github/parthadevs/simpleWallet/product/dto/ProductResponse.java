package io.github.parthadevs.simpleWallet.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer availableQuantity;
    private boolean isActive;
    private LocalDateTime createdAt;
}
