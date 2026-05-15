package com.cts.controller;


import jakarta.validation.Valid;


import org.springframework.web.bind.annotation.*;

import com.cts.entity.Product;
import com.cts.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //@PreAuthorize("hasAnyRole('Admin','WAREHOUSE','DOCTOR')")
    @GetMapping
    public List<Product> listProducts() {
        return productService.getAllProducts();
    }

    //@PreAuthorize("hasRole('Admin')")
    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        return productService.saveProduct(product);
    }
}
