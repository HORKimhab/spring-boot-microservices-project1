package com.reanit.ws.products.rest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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
    public ResponseEntity<Object> createProduct(@RequestBody CreateProductRestModel product){

        String productId;
        try {
            productId = productService.createProduct(product);
        } catch (Exception e) {
            // e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorMessage(new Date(), e.getMessage(), "/products"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }
}
