package com.example.Notification_Service.service;


import com.example.common.dto.OrderConfirmedEvent;
import com.example.common.dto.OrderPlacedEvent;
import com.example.common.dto.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationConsumer.class);


    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group")
    public void handleOrderCreated(OrderPlacedEvent event) {
        LOGGER.info("--- NOTIFICATION ---");
        LOGGER.info("Sending 'Order Received, Awaiting Payment' to user: {}", event.getUserId());
        LOGGER.info("Order ID: {}", event.getOrderId());
        LOGGER.info("Total Amount: {}", event.getTotalAmount());
        LOGGER.info("--------------------");
    }


    @KafkaListener(topics = "payment-processed-topic", groupId = "notification-group")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        LOGGER.info("--- NOTIFICATION ---");
        LOGGER.info("Sending Payment Status Update to user: {}", event.getUserId());
        LOGGER.info("Order ID: {}", event.getOrderId());
        LOGGER.info("Payment Status: {}", event.getPaymentStatus());
        LOGGER.info("Transaction ID: {}", event.getTransactionId());
        LOGGER.info("--------------------");
    }


    @KafkaListener(topics = "order-confirmed-topic", groupId = "notification-group")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        LOGGER.info("--- NOTIFICATION ---");
        LOGGER.info("Sending 'Order Confirmed!' notification for orderId: {}", event.getOrderId());
        LOGGER.info("Your order has been successfully processed and will be shipped soon.");
        LOGGER.info("--------------------");
    }
}