package com.example.watsonaiops;

import com.example.watsonaiops.schema.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class EdgeResolveProcessor {

    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, Node> nodeStream;
    KStream<String, Edge> edgeKStream;
    KStream<String, NodeAdjacent> nodeAdjStream;


    public void initialize(){
        loadNodeStream();
        loadEdgeStream();
        loadNodeAdjStream();
        bindEdge();

        build();

    }

    private void loadNodeStream(){
        // node
        nodeStream = streamsBuilder
            .stream(Topics.NODE_TOPOLOGICAL_DATA, Consumed.with(Serdes.String(), new NodeSerde()));
    }

    private void loadEdgeStream(){
        edgeKStream = streamsBuilder
                .stream(Topics.EDGE_TOPOLOGICAL_DATA, Consumed.with(Serdes.String(), new EdgeSerde()));
        edgeKStream
                .foreach((k, v)-> System.out.println("EDGE: "+v.getUniqueIdFrom() + " --> " + v.getUniqueIdTo()));
    }

    private void loadNodeAdjStream(){
        nodeAdjStream = streamsBuilder
            .stream(Topics.NODE_ADJACENT_LIST, Consumed.with(Serdes.String(), new NodeAdjacentSerde()));

        nodeAdjStream
            .foreach((k, nodeAdjacent)->{
                String list = nodeAdjacent
                        .getAdjNodes()
                        .stream()
                        .reduce("",(s, s2) -> s + s2);
                System.out.println("Node Adj for node: "
                        + nodeAdjacent.getRootNode().getUniqueId()
                        + " [" + list + "]"
                );
            });
    }

    private void bindEdge(){
        KStream<String, Edge> edgesMappedByFrom = edgeKStream
                .selectKey((s, edge) -> edge.getUniqueIdFrom());

        KStream<String, NodeAdjacent> mappedAdjNodes = nodeAdjStream
            .join(
                edgesMappedByFrom,
                (nodeAdjacent, edge) -> {
                    nodeAdjacent.addAdjNodeIfNotExists(edge.getUniqueIdTo());
                    return nodeAdjacent;
                },
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofDays(360)),
                    StreamJoined.with(
                        Serdes.String(),
                        new NodeAdjacentSerde(),
                        new EdgeSerde()
                    )
            );

        mappedAdjNodes
            .to(Topics.NODE_ADJACENT_LIST, Produced.with(Serdes.String(), new NodeAdjacentSerde()));
    }


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
