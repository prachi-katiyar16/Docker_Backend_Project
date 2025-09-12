package com.example.Cart_Service.model;

import lombok.Data;
import jakarta.validation.constraints.Min;

@Data
public class AddItemRequest {

    private Long productId;
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}


// This is a Data Transfer Object (DTO) that defines the exact structure of the JSON request body when a user adds an item to the cart.
