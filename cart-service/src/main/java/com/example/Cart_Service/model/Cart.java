package com.example.Cart_Service.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart implements Serializable {
    private String userId;
    private List<CartItem> items = new ArrayList<>();
    private double totalPrice;

    public Cart(String userId) {
        this.userId = userId;
    }

    public void recalculateTotalPrice() {
        this.totalPrice = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}


//This class represents a user's complete shopping cart
