package com.example.common.dto;


import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private double price;
    private int stock;
}


// This DTO defines the structure of the product data that the Cart service expects to receive from the Product microservice.