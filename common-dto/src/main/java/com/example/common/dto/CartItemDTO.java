package com.example.common.dto;


import lombok.Data;


@Data
public class CartItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
}