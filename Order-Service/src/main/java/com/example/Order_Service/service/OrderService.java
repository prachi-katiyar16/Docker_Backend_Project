package com.example.Order_Service.service;

import com.example.Order_Service.entity.Order;
import com.example.Order_Service.entity.OrderItem;
import com.example.Order_Service.model.*;
import com.example.Order_Service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${cart.service.url}")
    private String cartServiceUrl;

    public Order initiateOrder(String userId) {

        CartDTO cart = restTemplate.getForObject(cartServiceUrl + "/" + userId, CartDTO.class);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an empty order. Cart is empty for user: " + userId);
        }


        Order order = Order.builder()
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .totalAmount(cart.getTotalPrice())
                .orderStatus("PENDING_PAYMENT") // New status
                .build();

        order.setOrderItems(cart.getItems().stream().map(cartItem ->
                OrderItem.builder()
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getPrice())
                        .order(order)
                        .build()
        ).collect(Collectors.toList()));


        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalAmount(),
                savedOrder.getOrderItems().stream()
                        .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );
        kafkaTemplate.send("order-placed-topic", event);

        return savedOrder;
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "order-group")
    public void handlePaymentResult(PaymentProcessedEvent paymentEvent) {
        Order order = orderRepository.findById(paymentEvent.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found after payment processing: " + paymentEvent.getOrderId()));

        if ("SUCCESS".equals(paymentEvent.getPaymentStatus())) {

            order.setOrderStatus("CONFIRMED");
            orderRepository.save(order);

            OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                    order.getId(),
                    order.getOrderItems().stream()
                            .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity()))
                            .collect(Collectors.toList())
            );
            kafkaTemplate.send("order-confirmed-topic", confirmedEvent);


            CartItemRemovalRequest removalRequest = new CartItemRemovalRequest(
                    order.getOrderItems().stream().map(OrderItem::getProductId).collect(Collectors.toList())
            );
            restTemplate.postForObject(cartServiceUrl + "/items/delete-ordered", removalRequest, Void.class, order.getUserId());

        } else {

            order.setOrderStatus("PAYMENT_FAILED");
            orderRepository.save(order);

        }
    }



    public List<Order> getOrdersForUser(String userId, String userRole) {
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return orderRepository.findAll();
        } else {
            return orderRepository.findByUserId(userId);
        }
    }


    public Optional<Order> getOrderById(Long orderId, String userId, String userRole) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if ("ADMIN".equalsIgnoreCase(userRole) || order.getUserId().equals(userId)) {
                return orderOpt;
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
