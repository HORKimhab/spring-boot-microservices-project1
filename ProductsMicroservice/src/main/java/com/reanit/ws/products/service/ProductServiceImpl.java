package com.reanit.ws.products.service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.reanit.ws.products.rest.CreateProductRestModel;

@Service
public class ProductServiceImpl implements ProductService {

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate; 

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override 
    public String createProduct(CreateProductRestModel productRestModel){

        String productId = UUID.randomUUID().toString();
        String title = productRestModel.getTitle(); 
        BigDecimal price = productRestModel.getPrice(); 
        Integer quantity = productRestModel.getQuantity(); 
        
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId, title, price, quantity);

        // Send message to kafka 
        // kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        // Kafka send is asynchronous. The returned CompletableFuture completes when the broker
        // acknowledges the message or when the send fails.
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future = 
            kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        // Register a completion callback to log the outcome without blocking the request thread.
        future.whenComplete((result, exception) -> {
            if(exception != null){
                LOGGER.error("Failed to send message: " + exception.getMessage());
            }else {
                LOGGER.info("Message sent successfully: " + result.getRecordMetadata());
            }
        });

        // future.join();

        return productId;
    }
}
