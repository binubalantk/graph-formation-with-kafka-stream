package com.example.watsonaiops;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class KafkaStreamFactory {
    /**
     * Static Method to create KafkaStreams Object from given StreamBuilder and application id
     * @param streamsBuilder The StreamBuilder
     * @param appId The Application id
     * @return KafkaStream
     */
    public static KafkaStreams create(StreamsBuilder streamsBuilder, String appId){
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaStreams(streamsBuilder.build(), props);
    }
}
