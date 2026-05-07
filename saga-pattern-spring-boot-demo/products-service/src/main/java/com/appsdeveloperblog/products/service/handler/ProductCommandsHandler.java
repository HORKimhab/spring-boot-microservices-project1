package com.appsdeveloperblog.products.service.handler;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;

@Component
@KafkaListener(topics="${products.commands.topic.name}")
public class ProductCommandsHandler {

    @KafkaListener
    public void handleCommand(@Payload ReserveProductCommand command){
        
    }
}
