package com.reanit.ws.products;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.adapter.KafkaMessageHandlerMethodFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.reanit.ws.core.ProductCreatedEvent;
// import com.fasterxml.jackson.databind.JsonDeserializer;
import com.reanit.ws.products.rest.CreateProductRestModel;
import com.reanit.ws.products.service.ProductService;


/**
 * Integration test class for the Products service.
 *
 * <p>This test bootstraps the full Spring Boot application context and uses an
 * embedded Kafka cluster to simulate real messaging behavior during tests.</p>
 *
 * <p><b>Key configurations:</b></p>
 * <ul>
 *   <li>{@code @SpringBootTest} - Loads the full application context.</li>
 *   <li>{@code @EmbeddedKafka} - Spins up an in-memory Kafka cluster with
 *       3 brokers and 3 partitions for testing.</li>
 *   <li>{@code @ActiveProfiles("test")} - Uses {@code application-test.properties}
 *       for test-specific configuration.</li>
 *   <li>{@code @DirtiesContext} - Ensures the application context is reset after tests,
 *       preventing side effects between test runs.</li>
 *   <li>{@code @TestInstance(PER_CLASS)} - Uses a single test instance for all test methods,
 *       allowing shared setup/teardown logic.</li>
 * </ul>
 *
 * <p>The Kafka bootstrap servers are dynamically injected using the embedded Kafka
 * broker configuration.</p>
 *
 * <p><b>Note:</b> This class currently serves as a base for integration tests and
 * does not yet contain test methods.</p>
 */
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test") // application-test.properties
@EmbeddedKafka(partitions=3, count=3, controlledShutdown=true)
@SpringBootTest(properties="spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductsServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    Environment environment; 

    private KafkaMessageListenerContainer<String, ProductCreatedEvent> container; 
    private BlockingQueue<ConsumerRecord<String, ProductCreatedEvent>> records; 

    @BeforeAll 
    void setUp(){
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties()); 
        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("product-created-events-topic-name"));
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingDeque<>();
        container.setupMessageListener((MessageListener<String, ProductCreatedEvent>) records::add);

        ContainerTestUtils.waitForAssignment(containerProperties, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @Test
    void testCreateProduct_whenGivenValidProductDetails_successfullSendsKafakaMessage() throws Exception{

        // Arrange 
        String title="iphone 17 pro max"; 
        BigDecimal price = new BigDecimal(600);
        Integer quantity = 1; 

        CreateProductRestModel createProductRestModel = new CreateProductRestModel(); 
        createProductRestModel.setTitle(title);
        createProductRestModel.setPrice(price);
        createProductRestModel.setQuantity(quantity);

        // Act 
        productService.createProduct(createProductRestModel);

        // Assert
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(), 
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class, 
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class, 
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class,
            ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"),
            JacksonJsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset")
        ); 
    }

    @AfterAll
    void tearDown(){
        container.stop(); 
    }

}
