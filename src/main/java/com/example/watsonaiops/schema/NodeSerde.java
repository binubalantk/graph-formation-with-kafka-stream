package com.example.watsonaiops.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class NodeSerde implements Serializer<Node>, Deserializer<Node>, Serde<Node> {
    /**
     * The Jackson JSON Object mapper instance
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Node deserialize(String topic, byte[] data) {
        if(data == null){
            return null;
        }
        try{
            return OBJECT_MAPPER.readValue(data, Node.class);
        }catch (final Exception e){
            e.printStackTrace();
            throw new SerializationException(e);
        }
    }

    @Override
    public Serializer<Node> serializer() {
        return this;
    }

    @Override
    public Deserializer<Node> deserializer() {
        return this;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, Node data) {
        if (data == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (final Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
