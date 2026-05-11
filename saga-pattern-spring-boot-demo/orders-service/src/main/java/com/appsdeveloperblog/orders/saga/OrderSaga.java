package com.appsdeveloperblog.orders.saga;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.appsdeveloperblog.core.dto.commands.ApproveOrderCommand;
import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;

@Component
@KafkaListener(topics={
    "${orders.events.topic.name}",
    "${products.events.topic.name}",
    "${payments.events.topic.name}",
})
public class OrderSaga {

    @Value("${products.commands.topic.name}")
    private String productsCommandsTopicName;

    @Value("${payments.commands.topic.name}")
    private String paymentsCommandsTopicName;

    @Value("${orders.commands.topic.name}")
    private String ordersCommandsTopicName;

    private final KafkaTemplate<String, Object> kafkaTemplate; 
    private final OrderHistoryService orderHistoryService; 
    

    public OrderSaga(KafkaTemplate<String, Object> kafkaTemplate, OrderHistoryService orderHistoryService){
        this.kafkaTemplate = kafkaTemplate; 
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event){
        ReserveProductCommand command = new ReserveProductCommand(
            event.getProductId(), 
            event.getProductQuantity(), 
            event.getOrderId()
        ); 

        kafkaTemplate.send(productsCommandsTopicName, command);

         try {
           orderHistoryService.add(event.getOrderId(), OrderStatus.CREATED);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "[OrderSaga] Failed to add history order", ex
            );
        }
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent event){

        ProcessPaymentCommand processPaymentCommand = new ProcessPaymentCommand(
            event.getOrderId(), event.getProductId(), event.getProductPrice(), event.getProductQuantity()
        );

        kafkaTemplate.send(paymentsCommandsTopicName, processPaymentCommand);
    }

    @KafkaHandler 
    public void handleEvent(@Payload PaymentProcessedEvent event){
        
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(event.getOrderId());
        kafkaTemplate.send(ordersCommandsTopicName, approveOrderCommand);
    }

}
