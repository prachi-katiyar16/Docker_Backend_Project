package com.example.Payment_Service.service;

import com.example.Payment_Service.entity.Payment;
import com.example.Payment_Service.repository.PaymentRepository;
import com.example.common.dto.OrderPlacedEvent;
import com.example.common.dto.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    @KafkaListener(topics = "order-placed-topic", groupId = "payment-group")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        LOGGER.info("Received order placed event, processing payment for orderId: {}", event.getOrderId());


        boolean paymentSuccess = Math.random() > 0.1;
        String status = paymentSuccess ? "SUCCESS" : "FAILED";
        String transactionId = UUID.randomUUID().toString();


        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .amount(event.getTotalAmount())
                .paymentDate(LocalDateTime.now())
                .status(status)
                .transactionId(transactionId)
                .build();
        paymentRepository.save(payment);
        LOGGER.info("Payment record saved with status: {}", status);

        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(
                event.getOrderId(),
                event.getUserId(),
                status,
                transactionId,
                event.getAuthToken()
        );
        kafkaTemplate.send("payment-processed-topic", paymentEvent);
        LOGGER.info("Published payment processed event for orderId: {}", event.getOrderId());
    }
}