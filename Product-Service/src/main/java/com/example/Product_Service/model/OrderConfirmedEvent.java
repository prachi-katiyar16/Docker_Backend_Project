package com.example.Product_Service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmedEvent {
    private Long orderId;
    private List<OrderItemDTO> items;
}