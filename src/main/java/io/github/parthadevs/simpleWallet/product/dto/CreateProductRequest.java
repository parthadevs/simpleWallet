package io.github.parthadevs.simpleWallet.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(min = 2, max = 200, message = "Slug must be between 2 and 200 characters")
    private String slug;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must not be negative")
    private BigDecimal price;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    private Integer availableQuantity;

    private Boolean isActive;

    @Builder.Default
    private List<ProductStockItem> stockItems = List.of();
}
