package com.example.Cart_Service.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {

    @Id
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
}


//This class represents a single product item within the Cart