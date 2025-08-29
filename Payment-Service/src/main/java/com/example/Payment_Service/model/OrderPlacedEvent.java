package com.example.Payment_Service.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private String userId;
    private Double totalAmount;
    private List<OrderItemDTO> items;
}
