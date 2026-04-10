package com.reanit.ws.products;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.reanit.ws.core.ProductCreatedEvent;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.properties.delivery.timeout.ms}")
    private String deliveryTimeout;

    @Value("${spring.kafka.producer.properties.linger.ms}")
    private String linger;

    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private String requestTimeout;

    @Value("${spring.kafka.producer.properties.enable.idempotence}")
    private boolean idempotence; 

    @Value("${spring.kafka.producer.properties.max.in.flight.requests.per.connection}")
    private Integer inflightRequests; 

    /**
     * Creates and returns configuration properties for the Kafka producer.
     *
     * <p>This method initializes a {@link Map} containing key-value pairs
     * used to configure the Kafka producer. These configurations may include
     * settings such as bootstrap servers, serializers, retries, acknowledgments,
     * and other producer-related properties.</p>
     *
     * @return a {@link Map} containing Kafka producer configuration properties
    */
    Map<String, Object> producerConfigs() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
        config.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotence);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, inflightRequests);

        return config; 
    }

    @Bean
    ProducerFactory<String, ProductCreatedEvent> producerFactory(){
        return new DefaultKafkaProducerFactory<>(producerConfigs());  
    }

    /* 
        KafkaTemplate is a high-level abstraction provided by the Spring for Apache Kafka project to simplify 
        the process of sending messages to Kafka topics. It wraps the standard Apache Kafka Producer, offering
        thread-safe, convenience methods that integrate seamlessly with Spring's dependency injection and 
        configuration.
    */
    @Bean
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }


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
