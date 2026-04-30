package com.reanit.ws.products;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


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

    @Test
    void testCreateProduct_whenGivenValidProductDetails_successfullSendsKafakaMessage(){

    }

}
