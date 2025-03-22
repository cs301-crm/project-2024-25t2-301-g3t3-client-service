package com.cs301.client_service.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Service for producing messages to Kafka topics
 */
@Component
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Value("${spring.kafka.topic.c2c}")
    private String c2cTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Produces a message to the appropriate Kafka topic
     * @param key the message key
     * @param message the message to produce
     * @param isSuccessful whether the API call was successful
     */
    public void produceMessage(String key, Object message, boolean isSuccessful) {
        // Only publish when API calls did not have errors
        if (!isSuccessful) {
            logger.debug("Skipping Kafka message production due to unsuccessful API call");
            return;
        }

        logger.debug("Producing message to topic {}: {}", c2cTopic, message);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(c2cTopic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent message=[{}] with offset=[{}]",
                    message.toString(),
                    result.getRecordMetadata().offset());
            } else {
                logger.error("Unable to send message=[{}] due to : {}",
                    message.toString(),
                    ex.getMessage());
            }
        });
    }
    
    /**
     * Legacy method for backward compatibility
     * @param key the message key
     * @param message the message to produce
     */
    public void produceMessage(String key, Object message) {
        // By default, assume successful API call
        produceMessage(key, message, true);
    }
}
