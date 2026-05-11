package com.appsdeveloperblog.orders.service.handler;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.commands.ApproveOrderCommand;
import com.appsdeveloperblog.orders.service.OrderService;

@Component
@KafkaListener(topics="${order.commands.topic.name}")
public class OrderCommandsHandler {

    private final OrderService orderService; 

    public OrderCommandsHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener
    public void handleCommand(@Payload ApproveOrderCommand approveOrderCommand){
        orderService.approveOrder(approveOrderCommand.getOrderId());
    }

}
