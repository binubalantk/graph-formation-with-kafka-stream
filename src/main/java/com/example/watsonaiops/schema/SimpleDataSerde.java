package com.example.watsonaiops.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class SimpleDataSerde implements Serializer<SimpleData>, Deserializer<SimpleData>, Serde<SimpleData> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public SimpleData deserialize(String topic, byte[] data) {
        if(data == null){
            return null;
        }
        try{
            return OBJECT_MAPPER.readValue(data, SimpleData.class);
        }catch (final Exception e){
            e.printStackTrace();
            throw new SerializationException(e);
        }
    }

    @Override
    public Serializer<SimpleData> serializer() {
        return this;
    }

    @Override
    public Deserializer<SimpleData> deserializer() {
        return this;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, SimpleData data) {
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
