package com.cs301.client_service.kafka;

import com.cs301.client_service.protobuf.Log;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

/**
 * Test consumer for the Log Kafka topic.
 * Used for monitoring and debugging Log messages during testing.
 */
public class TestLogConsumer {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "log";
    private static final String GROUP_ID = "client-service-test-log-group";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8000";

    public static void main(String[] args) {
        System.out.println("Starting Log Test Consumer...");
        
        // Configure consumer properties
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID + "-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        props.put("specific.protobuf.value.type", Log.class.getName());

        // Create a consumer
        try (KafkaConsumer<String, Log> consumer = new KafkaConsumer<>(props)) {
            // Subscribe to topic
            consumer.subscribe(Collections.singletonList(TOPIC));
            System.out.println("Subscribed to topic: " + TOPIC);
            System.out.println("Starting to poll for messages...");
            
            // Poll for new messages
            while (true) {
                ConsumerRecords<String, Log> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, Log> record : records) {
                    printMessage(record);
                }
            }
        } catch (Exception e) {
            System.err.println("Error consuming messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Print the details of a Log message
     * 
     * @param record The Kafka consumer record containing the Log message
     */
    private static void printMessage(ConsumerRecord<String, Log> record) {
        System.out.println("\n=== Log Message Received ===");
        System.out.println("Key: " + record.key());
        System.out.println("Partition: " + record.partition());
        System.out.println("Offset: " + record.offset());
        
        Log log = record.value();
        System.out.println("\nMessage Content:");
        System.out.println("  Log ID: " + log.getLogId());
        System.out.println("  Actor: " + log.getActor());
        System.out.println("  Transaction Type: " + log.getTransactionType());
        System.out.println("  Action: " + log.getAction());
        System.out.println("  Timestamp: " + log.getTimestamp());
        
        System.out.println("=============================\n");
    }
}
