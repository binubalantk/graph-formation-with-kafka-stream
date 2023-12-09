package com.example.watsonaiops;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class KafkaStreamFactory {
    public static KafkaStreams create(StreamsBuilder streamsBuilder, String appId){
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
//        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
//        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000L);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaStreams(streamsBuilder.build(), props);
    }
}
