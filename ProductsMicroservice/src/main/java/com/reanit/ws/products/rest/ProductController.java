package com.reanit.ws.products.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reanit.ws.products.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    ProductService productService; 

    public ProductController(ProductService productService){
        this.productService = productService;
    }
    
    /**
     * Creates a new product from the request payload.
     *
     * @param product the product data sent in the request body
     * @return an HTTP 201 response when the product creation request is accepted
     */
    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody CreateProductRestModel product){

        String productId = productService.createProduct(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }
}
