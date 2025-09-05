package com.example.Order_Service.controller;

import com.example.Order_Service.entity.Order;
import com.example.Order_Service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping("/initiate")
    public ResponseEntity<Order> initiateOrder(@RequestHeader("X-Authenticated-Id") String userId,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
        return ResponseEntity.ok(orderService.initiateOrder(userId,authToken));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestHeader("X-Authenticated-Id") String userId,
            @RequestHeader("X-Authenticated-Role") String userRole) {

        List<Order> orders = orderService.getOrdersForUser(userId, userRole);
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("X-Authenticated-Id") String userId,
            @RequestHeader("X-Authenticated-Role") String userRole) {

        return orderService.getOrderById(orderId, userId, userRole)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}