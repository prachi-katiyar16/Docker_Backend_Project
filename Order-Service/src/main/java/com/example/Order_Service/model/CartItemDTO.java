package com.example.Order_Service.model;


import lombok.Data;

@Data
public class CartItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
}