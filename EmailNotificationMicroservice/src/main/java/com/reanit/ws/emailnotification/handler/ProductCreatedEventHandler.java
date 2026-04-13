package com.reanit.ws.emailnotification.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.reanit.ws.core.ProductCreatedEvent;
import com.reanit.ws.emailnotification.error.NotRetryableException;

@Component
@KafkaListener(
    topics = "${spring.kafka.consumer.product-created-topic}"
)
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handle(ProductCreatedEvent productCreatedEvent) {
        if(true) throw new NotRetryableException("An error took place. No need to conusme this message again.");
        LOGGER.info("Received a new event: " + productCreatedEvent.getTitle());
    }
}
 
