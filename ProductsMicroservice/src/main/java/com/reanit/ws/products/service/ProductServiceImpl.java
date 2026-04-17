package com.reanit.ws.products.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.reanit.ws.core.ProductCreatedEvent;
import com.reanit.ws.products.rest.CreateProductRestModel;

@Service
public class ProductServiceImpl implements ProductService {

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate; 
    private final String productCreatedTopic;
    private final String secondaryProductCreatedTopic;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductServiceImpl(
        KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate,
        @Value("${app.kafka.topics.product-created}") String productCreatedTopic,
        @Value("${app.kafka.topics.product-created-secondary}") String secondaryProductCreatedTopic
    ){
        this.kafkaTemplate = kafkaTemplate;
        this.productCreatedTopic = productCreatedTopic;
        this.secondaryProductCreatedTopic = secondaryProductCreatedTopic;
    }

    @Override 
    public String createProduct(CreateProductRestModel productRestModel) throws Exception {

        String productId = UUID.randomUUID().toString();
        String title = productRestModel.getTitle(); 
        BigDecimal price = productRestModel.getPrice(); 
        Integer quantity = productRestModel.getQuantity(); 
        
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId, title, price, quantity);

        // Send message to kafka 
        // kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        // Kafka send is asynchronous. The returned CompletableFuture completes when the broker
        // acknowledges the message or when the send fails.
        // CompletableFuture<SendResult<String, ProductCreatedEvent>> future = 
        //     kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        // // Register a completion callback to log the outcome without blocking the request thread.
        // future.whenComplete((result, exception) -> {
        //     if(exception != null){
        //         LOGGER.error("*** Failed to send message: " + exception.getMessage());
        //     }else {
        //         LOGGER.info("*** Message sent successfully: " + result.getRecordMetadata());
        //     }
        // });

        LOGGER.info("Before publishing a ProductCreatedEvent");

        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>(productCreatedTopic, productId, productCreatedEvent);

        record.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        /* 
            - Topic name
            - Partition 
            - Offset
            - Timestamp
        */

        // SendResult<String, ProductCreatedEvent> result = 
        //     kafkaTemplate.send(productCreatedTopic, productId, productCreatedEvent)
        //         .get();

        SendResult<String, ProductCreatedEvent> result = 
            kafkaTemplate.send(record).get();

                

        SendResult<String, ProductCreatedEvent> secondaryResult =
            kafkaTemplate.send(secondaryProductCreatedTopic, productId, productCreatedEvent)
                .get();

        // future.join();

        RecordMetadata recordMetadata = result.getRecordMetadata();
        RecordMetadata secondaryRecordMetadata = secondaryResult.getRecordMetadata();

        LOGGER.info("Partition: " + recordMetadata.partition());
        LOGGER.info("Topic: " + recordMetadata.topic());
        LOGGER.info("Offset: " + recordMetadata.offset());
        LOGGER.info("Secondary partition: " + secondaryRecordMetadata.partition());
        LOGGER.info("Secondary topic: " + secondaryRecordMetadata.topic());
        LOGGER.info("Secondary offset: " + secondaryRecordMetadata.offset());

        LOGGER.info("*** Returning product id");

        return productId;
    }
}
