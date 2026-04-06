# Kafka Producer Communication Style

This document explains how a Kafka producer communicates with a Kafka cluster and how that differs in synchronous and asynchronous sending styles.

## Core Flow

1. The producer application creates a record with:
   - topic
   - optional key
   - value
   - optional headers
2. Kafka producer serializes the key and value into bytes.
3. The producer asks the cluster for metadata to learn:
   - which brokers exist
   - which partitions belong to the topic
   - which broker is leader for each partition
4. The partitioner chooses a target partition.
   - If a key exists, the same key usually maps to the same partition.
   - If no key exists, Kafka can distribute messages across partitions.
5. The producer batches records per partition for efficiency.
6. The producer sends the batch to the leader broker for that partition.
7. The leader appends the record to its log.
8. Depending on the `acks` setting, the broker sends an acknowledgement after local write or replication.
9. The producer marks the send as successful or retries if configured and allowed.

## Communication Styles

### 1. Synchronous Send

In synchronous mode, the application waits for Kafka to acknowledge the message before continuing.

Typical Java style:

```java
SendResult<String, String> result =
    kafkaTemplate.send("topic1", "order-1", "created").get();
```

Characteristics:

- Simple control flow
- Easier to reason about success and failure at the call site
- Higher end-to-end latency
- Lower throughput if every send blocks the caller
- Useful when the next business step must only happen after the event is confirmed

## 2. Asynchronous Send

In asynchronous mode, the application sends the message and continues immediately. Success or failure is handled later through a callback or future completion.

Typical Java style:

```java
CompletableFuture<SendResult<String, String>> future =
    kafkaTemplate.send("topic1", "order-1", "created");

future.whenComplete((result, ex) -> {
    if (ex != null) {
        // handle failure
        return;
    }
    // handle success
});
```

Characteristics:

- Better throughput
- Lower blocking on the caller thread
- Better fit for high-volume event publishing
- Requires explicit success and error handling
- Harder to trace if callback handling is weak

## Metadata and Broker Interaction

Kafka producers do not send blindly to any broker forever.

- The producer first connects to a bootstrap server.
- It fetches topic metadata from the cluster.
- It learns the leader broker for the destination partition.
- It sends the actual record to that partition leader.
- If leadership changes, the producer refreshes metadata and resends when appropriate.

Because of this design, the bootstrap server is only an entry point. The final write may go to another broker.

## Delivery Guarantees and Acknowledgements

The communication style is also shaped by acknowledgement settings.

- `acks=0`
  The producer does not wait for broker acknowledgement. Fastest, but message loss is possible.
- `acks=1`
  The leader broker acknowledges after writing locally. Balanced speed and safety.
- `acks=all`
  The leader waits for in-sync replicas before acknowledging. Strongest durability among standard settings.

These settings apply to both synchronous and asynchronous sending. The difference is whether the application thread waits for the result directly.

## Retry Behavior

If a send fails because of a transient broker or network problem, the producer can retry.

Important settings:

- `retries`
- `delivery.timeout.ms`
- `request.timeout.ms`
- `retry.backoff.ms`
- `enable.idempotence=true`

Idempotence is important when reducing duplicate message risk during retries.

## Practical Recommendation

- Use asynchronous send by default for event-driven microservices.
- Use synchronous send only when the business flow must stop until Kafka confirms persistence.
- Prefer `acks=all` with idempotence enabled for safer production delivery.
- Use message keys when event ordering per entity matters.

## Summary

Kafka producer communication is leader-based, metadata-driven, and optimized through batching. The main application-level decision is whether to wait for acknowledgement synchronously or handle the result asynchronously after the send is dispatched.
