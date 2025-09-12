package com.example.Order_Service.service;

import com.example.Order_Service.entity.Order;
import com.example.Order_Service.entity.OrderItem;
import com.example.Order_Service.repository.OrderRepository;
import com.example.common.dto.*;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public Order initiateOrder(String userId, String authToken) {
        HttpHeaders headers = new HttpHeaders();

        if (authToken != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authToken); // forward token exactly as received
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<CartDTO> response = restTemplate.exchange(
                cartServiceUrl,
                HttpMethod.GET,
                entity,
                CartDTO.class
        );

// Get the body
        CartDTO cart = response.getBody();
        Order order = Order.builder()
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .totalAmount(cart.getTotalPrice())
                .orderStatus("PENDING_PAYMENT")
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

        List<OrderItemDTO> dtoList = savedOrder.getOrderItems().stream()
                .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());


        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalAmount(),
                dtoList,
                authToken
        );

        kafkaTemplate.send("order-placed-topic", event);


        return savedOrder;
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "order-group")
    @Transactional
    public void handlePaymentResult(PaymentProcessedEvent paymentEvent) {
        Order order = orderRepository.findById(paymentEvent.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found after payment processing: " + paymentEvent.getOrderId()));

        if ("SUCCESS".equals(paymentEvent.getPaymentStatus())) {

            order.setOrderStatus("CONFIRMED");


            OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                    order.getId(),
                    order.getOrderItems().stream()
                            .map(item -> new OrderItemDTO(item.getProductId(), item.getQuantity()))
                            .collect(Collectors.toList())
            );
            kafkaTemplate.send("order-confirmed-topic", confirmedEvent);

            String authHeader=paymentEvent.getAuthToken();

            HttpHeaders headers=new HttpHeaders();
            if (authHeader!=null){
                headers.set(HttpHeaders.AUTHORIZATION,authHeader);
            }

            CartItemRemovalRequest removalRequest = new CartItemRemovalRequest(
                    order.getOrderItems().stream().map(OrderItem::getProductId).collect(Collectors.toList())
            );
            HttpEntity<CartItemRemovalRequest> requestEntity =new HttpEntity<>(removalRequest,headers);

            restTemplate.exchange(
                    cartServiceUrl + "/items/delete-ordered",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

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
