package com.reanit.ws.products;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

     /**
     * Creates Kafka topic for Product Created events.
     *
     * <p><b>Topic Name:</b> product-created-events-topic</p>
     *
     * <p><b>Configuration Details:</b></p>
     * <ul>
     *     <li><b>Partitions:</b> 3  
     *     Enables parallel processing of messages.</li>
     *
     *     <li><b>Replication Factor:</b> 3  
     *     Each partition is replicated across 3 Kafka brokers
     *     to ensure fault tolerance and high availability.</li>
     *
     *     <li><b>min.insync.replicas:</b> 2  
     *     Requires at least 2 replicas to acknowledge writes
     *     before Kafka accepts a message.</li>
     * </ul>
     *
     * <p><b>Behavior:</b></p>
     * This bean is automatically executed during Spring Boot
     * application startup. If the topic does not exist,
     * it will be created automatically.
     *
     * <p><b>Requirements:</b></p>
     * Kafka cluster must have at least 3 brokers to support
     * replication factor of 3.
     *
     * @return NewTopic configuration for product-created-events-topic
     */
    @Bean
    NewTopic createTopic(){
        return TopicBuilder.name("product-created-events-topic")
            .partitions(3)
            .replicas(3)
            .configs(Map.of("min.insync.replicas", "2"))
            .build();
    }
}
