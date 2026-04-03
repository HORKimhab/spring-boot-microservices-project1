# spring-boot-microservices-project1
Apache Kafka for Event-Driven Spring Boot Microservices

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

<!-- Delete logs -->
rm -rf /tmp/server-{1,2,3}/kraft-combined-logs

<!-- List topics -->
bash bin/kafka-topics.sh --list --bootstrap-server localhost:9092

<!-- Describe topic -->
bash bin/kafka-topics.sh --describe  --bootstrap-server localhost:9092

 Replicas: 2,3,1 is Broker ID

<!-- Describe topic -->
bash bin/kafka-topics.sh --delete --topic topic1 --bootstrap-server localhost:9092

<!-- Send message -->

bash bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9094 --topic new-topic 

<!-- Send message in key pairs -->
bash bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9094 --topic new-topic --property "parse.key=true" --property "key.separator=:"

bash bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092,localhost:9094 \
  --topic topic1 \
  --command-property "parse.key=true" \
  --command-property "key.separator=:"

<!-- Consume topic from beginning -->
bin/kafka-console-consumer.sh \
  --topic topic1 \
  --from-beginning \
  --bootstrap-server localhost:9092

<!-- Consume topic from latest -->
bin/kafka-console-consumer.sh \
  --topic topic1 \
  --bootstrap-server localhost:9092