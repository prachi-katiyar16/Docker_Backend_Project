package com.example.Cart_Service.service;

import com.example.Cart_Service.exception.InsufficientStockException;
import com.example.Cart_Service.exception.ProductNotFoundException;
import com.example.Cart_Service.model.AddItemRequest;
import com.example.Cart_Service.model.Cart;
import com.example.Cart_Service.model.CartItem;

import com.example.Cart_Service.repository.CartRepository;
import com.example.common.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;
    public Cart addItemToCart(String userId, String userRole, AddItemRequest itemRequest) {

        ProductDTO product = fetchProductDetails(itemRequest.getProductId(), userId, userRole);


        Cart cart = repository.findById(userId).orElse(new Cart(userId));


        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(itemRequest.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existingItem -> updateItemQuantity(existingItem, itemRequest.getQuantity(), product.getStock()),
                        () -> addNewItemToCart(cart, itemRequest.getQuantity(), product)
                );


        cart.recalculateTotalPrice();
        return repository.save(cart);
    }

    private ProductDTO fetchProductDetails(Long productId, String userId, String userRole) {
        String url = productServiceUrl + productId;


        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Authenticated-Id", userId);
        headers.set("X-Authenticated-Role", userRole);


        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {

            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ProductDTO.class
            );

            if (response.getBody() == null) {
                throw new ProductNotFoundException("Could not fetch product details for id: " + productId);
            }
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }
    }

    private void updateItemQuantity(CartItem existingItem, int quantityToAdd, int availableStock) {
        int newQuantity = existingItem.getQuantity() + quantityToAdd;

        if (availableStock < newQuantity) {
            throw new InsufficientStockException("Insufficient stock. Requested total: " + newQuantity + ", Available: " + availableStock);
        }
        existingItem.setQuantity(newQuantity);
    }

    private void addNewItemToCart(Cart cart, int quantity, ProductDTO product) {

        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Requested: " + quantity + ", Available: " + product.getStock());
        }
        CartItem newItem = new CartItem(
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice()
        );
        cart.getItems().add(newItem);
    }



    public Cart getCartByUserId(String userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new ProductNotFoundException("Cart not found for user: " + userId));
    }

    public void clearCart(String userId) {
        repository.deleteById(userId);
    }

    public void removeItemsFromCart(String userId, List<Long> productIds) {
        Cart cart = repository.findById(userId)
                .orElseThrow(() -> new ProductNotFoundException("Cart not found for user: " + userId));

        List<CartItem> itemsToKeep = cart.getItems().stream()
                .filter(item -> !productIds.contains(item.getProductId()))
                .collect(Collectors.toList());

        cart.setItems(itemsToKeep);
        cart.recalculateTotalPrice();
        repository.save(cart);
    }

}



// containing all the business rules and logic for managing a cart.
