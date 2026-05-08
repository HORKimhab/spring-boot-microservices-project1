package com.appsdeveloperblog.payments.service.handler;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;

@Component
@KafkaListener(topics="${payments.commands.topic.name}")
public class PaymentsCommandsHandler {

    @KafkaHandler
    public void handleCommand(@Payload ProcessPaymentCommand command){
        
    }
}
