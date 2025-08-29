package com.example.Order_Service.model;

import lombok.Data;
import java.util.List;

@Data
public class CartDTO {
    private String userId;
    private List<CartItemDTO> items;
    private double totalPrice;
}