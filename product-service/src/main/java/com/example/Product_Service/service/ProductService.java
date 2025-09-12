package com.example.Product_Service.service;

import com.example.Product_Service.entity.Product;
import com.example.Product_Service.exception.InsufficientStockException;
import com.example.Product_Service.exception.ResourceNotFoundException;
import com.example.Product_Service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;
    public Product addProduct(Product product) {
        return repository.save(product);
    }


    public Product updateProduct(Long id, Product updated) {
        return repository.findById(id)
                .map(product -> {
                    product.setName(updated.getName());
                    product.setDescription(updated.getDescription());
                    product.setPrice(updated.getPrice());
                    product.setStock(updated.getStock());
                    return repository.save(product);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }


    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }


    public List<Product> getAllProducts() {
        return repository.findAll();
    }


    public Optional<Product> getProductById(Long id) {
        return repository.findById(id);
    }


    public void reduceStock(Long productId, int quantity) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product id: " + productId +
                    ". Requested: " + quantity + ", Available: " + product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        repository.save(product);
    }
}
