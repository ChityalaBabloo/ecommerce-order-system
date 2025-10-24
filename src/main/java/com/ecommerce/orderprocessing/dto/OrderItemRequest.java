package com.ecommerce.orderprocessing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Order item details")
public class OrderItemRequest {
    
    @NotBlank(message = "Product name is required")
    @Schema(description = "Name of the product", example = "Laptop", required = true)
    private String productName;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of the product", example = "2", required = true, minimum = "1")
    private Integer quantity;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    @Schema(description = "Price per unit", example = "999.99", required = true, minimum = "0")
    private BigDecimal price;
}
