package com.example.watsonaiops;

import com.example.watsonaiops.schema.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;

import java.util.concurrent.CountDownLatch;

public class EdgeResolveProcessor {
    /**
     * The StreamBuilder Object
     */
    StreamsBuilder streamsBuilder = new StreamsBuilder();

    /**
     * Kafka KStream for Nodes
     */
    KStream<String, Node> nodeStream;

    /**
     * Kafka KStream for Edges
     */
    KStream<String, Edge> edgeKStream;

    /**
     * Kafka GlobalKTable for Node Adjacent list
     */
    GlobalKTable<String, NodeAdjacent> nodeAdjGTable;

    /**
     * Method to initialize the Edge resolver processor topology
     */
    public void initialize(){
        loadNodeStream();
        loadEdgeStream();
        loadNodeAdjGTable();
        bindEdge();

        build();

    }

    /**
     * Method to load Node stream
     */
    private void loadNodeStream(){
        // node
        nodeStream = streamsBuilder
            .stream(Topics.NODE_TOPOLOGICAL_DATA, Consumed.with(Serdes.String(), new NodeSerde()));
    }

    /**
     * Method to load Edge stream
     */
    private void loadEdgeStream(){
        edgeKStream = streamsBuilder
                .stream(Topics.EDGE_TOPOLOGICAL_DATA, Consumed.with(Serdes.String(), new EdgeSerde()));
        edgeKStream
                .foreach((k, v)-> System.out.println("EDGE: "+v.getUniqueIdFrom() + " --> " + v.getUniqueIdTo()));
    }

    /**
     * Method to load Node Adjacent list as GlobalKTable
     */
    private void loadNodeAdjGTable(){
        nodeAdjGTable = streamsBuilder
            .globalTable(Topics.NODE_ADJACENT_LIST, Consumed.with(Serdes.String(), new NodeAdjacentSerde()));
    }

    /**
     * Method to Bind Nodes and add Node Adjacent entries based on new Edge definition
     */
    private void bindEdge(){
        KStream<String, NodeAdjacent> mappedAdjNodes = edgeKStream
            .selectKey((s, edge) -> edge.getUniqueIdFrom())
            .join(
                nodeAdjGTable,
                (s, edge) -> s,
                (edge, nodeAdjacent) -> {
                    nodeAdjacent.addAdjNodeIfNotExists(edge.getUniqueIdTo());
                    return nodeAdjacent;
                }
            );

        mappedAdjNodes
            .to(Topics.NODE_ADJACENT_LIST, Produced.with(Serdes.String(), new NodeAdjacentSerde()));
    }

    /**
     * Method to build the Kafka Stream Processor Topology
     */
    private void build(){
        final KafkaStreams kafkaStreams = KafkaStreamFactory.create(streamsBuilder, "streams-watsonaiops-edge-resolve");
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        kafkaStreams.setUncaughtExceptionHandler(throwable -> {
            throwable.printStackTrace();
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        Runtime.getRuntime().addShutdownHook(new Thread("watsonaiops-shutdown-hook-er"){
            @Override
            public void run() {
                kafkaStreams.close();
                countDownLatch.countDown();
            }
        });

        try{
            kafkaStreams.start();
            countDownLatch.await();
        }catch (final Throwable e){
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}
