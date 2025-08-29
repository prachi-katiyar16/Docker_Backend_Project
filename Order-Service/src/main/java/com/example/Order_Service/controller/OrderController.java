package com.example.Order_Service.controller;

import com.example.Order_Service.entity.Order;
import com.example.Order_Service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping("/initiate")
    public ResponseEntity<Order> initiateOrder(@RequestHeader("X-Authenticated-User-Id") String userId) {
        return ResponseEntity.ok(orderService.initiateOrder(userId));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestHeader("X-Authenticated-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {

        List<Order> orders = orderService.getOrdersForUser(userId, userRole);
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("X-Authenticated-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {

        return orderService.getOrderById(orderId, userId, userRole)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}