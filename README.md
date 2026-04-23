# spring-boot-microservices-project1
Apache Kafka for Event-Driven Spring Boot Microservices

## Setup

- bash kafka.sh format 

<!-- Start Kafka instances 1–3 manually from terminal -->
Run the following commands:

- bash kafka.sh start1
- bash kafka.sh start2
- bash kafka.sh start3

Start spring boot 
- cd ProductsMicroservice && bash run 

List specific topic 'product-created-events-topic'
- bash bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic product-created-events-topic

## Resource

- https://kafka.apache.org/community/downloads/ - version: 4.2.0
- Udemy: https://www.udemy.com/course/apache-kafka-for-spring-boot-microservices

# Single-node KRaft

```bash
bash bin/kafka-storage.sh format \
  -t jDe9QDCvSxChxBxHN7tRmg \
  -c config/server.properties \
  --standalone

bash bin/kafka-server-start.sh config/server.properties
```

# Three-node KRaft quorum

<!-- Do not use `--standalone` for a 3-node controller quorum. Use the same
`--initial-controllers` value when formatting all three nodes. -->


CLUSTER_ID=GZr1B__JQ7Ozviiig_UBeg
DIR_ID_1=$(bash bin/kafka-storage.sh random-uuid)
DIR_ID_2=$(bash bin/kafka-storage.sh random-uuid)
DIR_ID_3=$(bash bin/kafka-storage.sh random-uuid)

INITIAL_CONTROLLERS="1@localhost:9093:${DIR_ID_1},2@localhost:9095:${DIR_ID_2},3@localhost:9097:${DIR_ID_3}"

KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"
bash bin/kafka-storage.sh format \
  -t "rO_gM3IJRVy6q4u-ESGU3Q" \
  -c config/server-1.properties

bash bin/kafka-storage.sh format \
  -t "rO_gM3IJRVy6q4u-ESGU3Q" \
  -c config/server-2.properties 

bash bin/kafka-storage.sh format \
 -t "rO_gM3IJRVy6q4u-ESGU3Q" \
  -c config/server-3.properties 

<!-- Start server  -->
bash bin/kafka-server-start.sh config/server-1.properties
bash bin/kafka-server-start.sh config/server-2.properties
bash bin/kafka-server-start.sh config/server-3.properties

CLUSTER_ID=$(bin/kafka-storage.sh random-uuid)

bin/kafka-storage.sh format -t $CLUSTER_ID -c config/server-1.properties
bin/kafka-storage.sh format -t $CLUSTER_ID -c config/server-2.properties
bin/kafka-storage.sh format -t $CLUSTER_ID -c config/server-3.properties

<!-- Stop server -->
bash bin/kafka-server-stop.sh

<!-- Create topic -->
bash bin/kafka-topics.sh --create --topic topic1 --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092,localhost:9094

bash bin/kafka-topics.sh --create --topic topic2 --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092,localhost:9094

<!-- Create topic, min.insync.replicas -->
bash bin/kafka-topics.sh --create --topic insync-topic --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092 --config min.insync.replicas=2

<!-- Delete logs -->
rm -rf /tmp/server-{1,2,3}/kraft-combined-logs

<!-- List topics -->
bash bin/kafka-topics.sh --list --bootstrap-server localhost:9092

<!-- Describe topic -->
bash bin/kafka-topics.sh --describe  --bootstrap-server localhost:9092

 Replicas: 2,3,1 is Broker ID

bash bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic {topicName}

bash bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic product-created-events-topic


<!-- Delete topic -->
bash bin/kafka-topics.sh --delete --topic topic1 --bootstrap-server localhost:9092

<!-- Send message -->

bash bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9094 --topic new-topic 

<!-- Send message in key pairs -->
bash bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9094 --topic new-topic --property "parse.key=true" --property "key.separator=:"

bash bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic topic1 \
  --reader-property "parse.key=true" \
  --reader-property "key.separator=:"

<!-- Consume topic from beginning -->
bin/kafka-console-consumer.sh \
  --topic topic1 \
  --from-beginning \
  --bootstrap-server localhost:9092

<!-- Consume topic from latest -->
bin/kafka-console-consumer.sh \
  --topic topic1 \
  --bootstrap-server localhost:9092

bash bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic topic1 \
  --formatter-property print.key=true \
  --formatter-property key.separator="        :       "

bash bin/kafka-console-consumer.sh \
  --topic product-created-events-topic \
  --bootstrap-server localhost:9092,localhost:9094 \
  --formatter-property print.key=true \
  --formatter-property key.separator="        :       "


bash bin/kafka-console-consumer.sh \
  --topic topic2 \
  --bootstrap-server localhost:9092,localhost:9094 \
  --formatter-property print.key=true \
  --formatter-property key.separator="        :       "


## Kafka Producer 

- Publish/Produce Messages 
- Serialize to binary format
- Specify topic name
- Specify topic partition 
- No response - if the producer is configured with acks=0 
- Acknowledgement (ACK) of Successful Storage 
- Non-Retryable Error - A permanent problem that is unlikely to be resolved by retrying the spend operation

  ### Communication Style

  - Synchronous
  - Asynchronous

## Init Apache Kafka Local Transactions With KafkaTemplate

This project initializes local Kafka transactions in `TransferService` by combining:

- a producer transaction id prefix
- a transaction-capable `DefaultKafkaProducerFactory`
- `KafkaTemplate`
- `KafkaTransactionManager`
- `KafkaTemplate.executeInTransaction(...)`

Configuration used by this project:

```properties
spring.kafka.producer.transaction-id-prefix=transfer-service-${random.value}-
```

Producer and transaction manager setup:

```java
@Bean
ProducerFactory<String, Object> producerFactory() {
    DefaultKafkaProducerFactory<String, Object> producerFactory =
            new DefaultKafkaProducerFactory<>(producerConfigs());
    producerFactory.setTransactionIdPrefix(transactionIdPrefix);
    return producerFactory;
}

@Bean
KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
}

@Bean
KafkaTransactionManager<String, Object> kafkaTransactionManager(
        ProducerFactory<String, Object> producerFactory) {
    return new KafkaTransactionManager<>(producerFactory);
}
```

Service usage:

```java
public boolean transfer(TransferRestModel transferRestModel) {
    kafkaTemplate.executeInTransaction(operations -> {
        operations.send("withdraw-money-topic", withdrawalEvent);
        callRemoteService();
        operations.send("deposit-money-topic", depositEvent);
        return true;
    });
    return true;
}
```

Important behavior:

- Kafka creates a transactional producer when the transaction starts
- records sent with `KafkaTemplate` stay invisible to `READ_COMMITTED` consumers until commit
- if the method throws an exception, the transaction is rolled back and consumers do not receive those records

In this project, that is why a failed `POST /transfers` no longer produces a visible withdrawal event in `WithdrawalService`.

## Note

<!-- Waits for an acknowledgement from all brokers -->
- spring.kafka.producer.acks=all
- replication-factor 5 
- config min.insync.replicas=2

<!-- Waits for an acknowledgement from a a leader broker only -->
- spring.kafka.producer.acks=1

<!-- Does not wait for an acknowledgement -->
- spring.kafka.producer.acks=0

<!--  How many times Kafka Producer will try to send a message before marking it as failed.  
      Default value is 2147483647.
-->
- spring.kafka.producer.retries=10

<!--  How long the producer will wait before attempting to retry a failed request.  
      Default value is 100 ms. 
-->
- spring.kafka.producer.properties.retry.backoff.ms=1000

<!--  The maximum time Producer can spend trying to deliver the message. 
      Default value is 120000 ms (2 mintues).
-->
- spring.kafka.producer.properties.delivery.timeout.ms=120000 

delivery.timeout.ms >= linger.ms + request.timeout.ms 

<!--  The maximum time in milliseconds that the producer will wait and buffer data before sending
      a batch of message. The default value is 0. 
-->
spring.kafka.producer.proerties.linger.ms=0

<!--  The maximum time to wait for a response from the broker after sending a request. 
      The default value is 30000 ms. 
-->
spring.kafka.producer.request.timeout.ms=30000

<!-- Config min.insync.replicas -->

bash bin/kafka-configs.sh \
  --bootstrap-server localhost:9092 \
  --alter \
  --entity-type topics \
  --entity-name topic2 \
  --add-config min.insync.replicas=2

bash bin/kafka-topics.sh --delete --topic __consumer_offsets --bootstrap-server localhost:9092

- 08. Trying how Kafka producer retries work, bash kafka.sh start1 

## Kafka Consumer

- A Kafka consumer reads records from a topic by polling Kafka brokers.
- Kafka tracks consumer progress with offsets, stored per consumer group.
- If multiple consumers use the same group id, Kafka distributes partitions across them.
- If different consumer groups read the same topic, each group receives its own copy of the stream.

## TransferService Transaction Behavior

When `POST /transfers` is handled by `TransferService`, both Kafka sends run inside a Kafka transaction.

Current flow in `TransferService`:

1. Send `WithdrawalRequestedEvent` to `withdraw-money-topic`
2. Call remote service
3. Send `DepositRequestedEvent` to `deposit-money-topic`

If the request fails before the method completes successfully, the Kafka transaction is rolled back.

That means:

- `WithdrawalService` will not receive the withdrawal event anymore
- `DepositService` will not receive the deposit event
- Kafka may show producer transaction logs, but consumers using `READ_COMMITTED` will not see rolled-back records

This is expected with the current configuration because:

- `TransferService` uses a transactional Kafka producer
- `WithdrawalService` uses `spring.kafka.consumer.isolation-level=READ_COMMITTED`

So if `POST /transfers` fails, `Received a new withdrawal event` should not appear in `WithdrawalService` logs.

Only when the transfer flow completes and the Kafka transaction commits will `WithdrawalService` receive the event.

### Consume Messages From This Project

Current project behavior:

- `KafkaConfig#createTopic()` creates `product-created-events-topic`
- `ProductServiceImpl#createProduct()` currently publishes to `topic2`

Consume from the topic currently used by the Spring Boot service:

```bash
bash bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic topic2 \
  --from-beginning
```

Consume and show message keys:

```bash
bash bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic topic2 \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" : "
```

Consume from `product-created-events-topic` if you switch the producer back to that topic:

```bash
bash bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic product-created-events-topic \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" : "

# List dead letter topic
bash bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic product-created-events-topic.DLT \
  --from-beginning \
  --formatter-property print.key=true \
  --formatter-property print.value=true

bash bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic product-created-events-topic-dlt \
  --from-beginning \
  --formatter-property print.key=true \
  --formatter-property print.value=true
```

```bash
# Console producer
bash bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic product-created-events-topic \
  --reader-property "parse.key=true" \
  --reader-property "key.separator=:"
```

### Consumer Group Example

Read with a named consumer group:

```bash
bash bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic topic2 \
  --group product-created-events-consumer-group \
  --from-beginning
```

Describe the consumer group and inspect committed offsets:

```bash
bash bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --describe \
  --group product-created-events-consumer-group
```

### Notes

- `--from-beginning` replays existing records only when the consumer group has no committed offset yet.
- Once offsets are committed for a group, the next run resumes from the last committed position.
- Offsets are stored in Kafka, typically in the `__consumer_offsets` internal topic.
- With 3 partitions, Kafka can actively assign work to up to 3 consumers in the same group for that topic.
- Access h2-console-db: http://localhost:{Port_Number}/h2-console
- Continue: 15.09 (1.48)
