package com.example.Cart_Service.controller;

import com.example.Cart_Service.model.AddItemRequest;
import com.example.Cart_Service.model.Cart;
import com.example.Cart_Service.model.CartItemRemovalRequest;
import com.example.Cart_Service.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService service;


    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestHeader("X-Authenticated-User-Id") String userId) {
        return ResponseEntity.ok(service.getCartByUserId(userId));
    }


    public ResponseEntity<Cart> addItem(@RequestHeader("X-Authenticated-User-Id") String userId,
                                        @RequestHeader("X-User-Role") String userRole,
                                        @RequestBody AddItemRequest itemRequest) {
        return ResponseEntity.ok(service.addItemToCart(userId, userRole, itemRequest));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("X-Authenticated-User-Id") String userId) {
        service.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/delete-ordered")
    public ResponseEntity<Void> removeOrderedItems(@RequestHeader("X-Authenticated-User-Id") String userId,
                                                   @RequestBody CartItemRemovalRequest request) {
        service.removeItemsFromCart(userId, request.getProductIds());
        return ResponseEntity.ok().build();
    }
}


// define the REST API endpoints that clients (like a frontend application) will use to interact with the Cart service.