package com.example.Product_Service.service;

import com.example.Product_Service.model.OrderConfirmedEvent; // CHANGED IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StockUpdateConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockUpdateConsumer.class);

    @Autowired
    private ProductService productService;

    @KafkaListener(topics = "order-confirmed-topic", groupId = "product-stock-group")
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        LOGGER.info("Received order confirmed event for orderId: {}", event.getOrderId());
        try {
            event.getItems().forEach(item -> {
                LOGGER.info("Reducing stock for productId: {} by quantity: {}", item.getProductId(), item.getQuantity());
                productService.reduceStock(item.getProductId(), item.getQuantity());
            });
            LOGGER.info("Stock successfully updated for orderId: {}", event.getOrderId());
        } catch (Exception e) {
            LOGGER.error("Error processing stock reduction for orderId: {}. Error: {}", event.getOrderId(), e.getMessage());
        }
    }
}