package com.example.Cart_Service.repository;

import com.example.Cart_Service.model.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CartRepository {
    private static final String HASH_KEY = "CART";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Cart save(Cart cart) {
        redisTemplate.opsForHash().put(HASH_KEY, cart.getUserId(), cart);
        return cart;
    }

    public Optional<Cart> findById(String userId) {
        Cart cart = (Cart) redisTemplate.opsForHash().get(HASH_KEY, userId);
        return Optional.ofNullable(cart);
    }

    public void deleteById(String userId) {
        redisTemplate.opsForHash().delete(HASH_KEY, userId);
    }
}


// This class is responsible for all database operations for the cart, specifically communicating with Redis