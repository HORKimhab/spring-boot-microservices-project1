package com.appsdeveloperblog.payments.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.Payment;
import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.exceptions.CreditCardProcessorUnavailableException;
import com.appsdeveloperblog.payments.service.PaymentService;

@Component
@KafkaListener(topics="${payments.commands.topic.name}")
public class PaymentsCommandsHandler {

    @Value("${payments.events.topic.name}")
    private String paymentEventsTopicName; 

    private final PaymentService paymentService; 
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate; 

    public PaymentsCommandsHandler(PaymentService paymentService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaHandler
    public void handleCommand(@Payload ProcessPaymentCommand command){

        try {
            Payment payment = new Payment(
                command.getOrderId(), 
                command.getProductId(), 
                command.getProductPrice(), 
                command.getProductQuantity()
            );
    
            Payment processedPayment = paymentService.process(payment);
            PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(processedPayment.getOrderId(), processedPayment.getId());
            kafkaTemplate.send(paymentEventsTopicName, paymentProcessedEvent);
 
        } catch (CreditCardProcessorUnavailableException e) {
            logger.error(e.getLocalizedMessage(), e);

            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(command.getOrderId(), 
                command.getProductId(), 
                command.getProductQuantity());
            
            kafkaTemplate.send(paymentEventsTopicName, paymentFailedEvent);
        }
    }
}
