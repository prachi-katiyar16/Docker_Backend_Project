package com.example.Notification_Service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessedEvent {
    private Long orderId;
    private String userId;
    private String paymentStatus;
    private String transactionId;
}