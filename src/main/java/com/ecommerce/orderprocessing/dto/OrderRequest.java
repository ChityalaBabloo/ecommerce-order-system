package com.ecommerce.orderprocessing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating a new order")
public class OrderRequest {
    
    @NotBlank(message = "Customer name is required")
    @Schema(description = "Customer's full name", example = "John Doe", required = true)
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Customer's email address", example = "john.doe@example.com", required = true)
    private String customerEmail;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @Schema(description = "List of items in the order", required = true)
    private List<OrderItemRequest> items;
}
