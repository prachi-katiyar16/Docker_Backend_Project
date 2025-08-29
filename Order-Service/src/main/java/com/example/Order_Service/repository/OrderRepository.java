package com.example.Order_Service.repository;

import com.example.Order_Service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
          List<Order> findByUserId(String userId);
}
