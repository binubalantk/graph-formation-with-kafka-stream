package com.example.watsonaiops;

import com.example.watsonaiops.schema.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class MasterProcessor {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, Node> nodeStream;
    KStream<String, Alert> alertStream;

    GlobalKTable<String, String> ipNodeMapGTable;
    GlobalKTable<String, String> hostNodeMapGTable;
    GlobalKTable<String, NodeAdjacent> adjGTable;


    public void initialize(){
        loadNodeStream();
        loadAlerts();

        loadGlobalTables();

        createNodeAdjLists();

        mapIpToNodeId();
        mapHostToNodeId();

        matchAlertWithNodeByIp();
        matchAlertWithNodeByHost();

        printAlertNodeMap();

        build();

    }

    private void loadNodeStream(){
        // node
        nodeStream = streamsBuilder
            .stream(Topics.NODE_TOPOLOGICAL_DATA, Consumed.with(Serdes.String(), new NodeSerde()));

        nodeStream
            .foreach((k, v)-> System.out.println("NODE: "+v.getUniqueId()));
    }

    private void loadAlerts(){
        alertStream = streamsBuilder
            .stream(Topics.ALERT, Consumed.with(Serdes.String(), new AlertSerde()));

        alertStream
            .foreach((k, v)-> System.out.println("ALERT: "+v.getUniqueId()));
    }

    private void mapIpToNodeId(){
        // node to ip address map
        nodeStream
            .map((k, node) -> KeyValue.pair(node.getResource().getIpAddress(), node.getUniqueId()))
            .to(Topics.NODEID_IP_MAP, Produced.with(Serdes.String(), Serdes.String()));
    }

    private void mapHostToNodeId(){
        // node to hostname map
        nodeStream
            .map((k, node) -> KeyValue.pair(node.getResource().getHostName(), node.getUniqueId()))
            .to(Topics.NODEID_HOST_MAP, Produced.with(Serdes.String(), Serdes.String()));
    }

    private void loadGlobalTables(){
        ipNodeMapGTable = streamsBuilder
        .globalTable(Topics.NODEID_IP_MAP,
                    Consumed.with(Serdes.String(), Serdes.String()));

        hostNodeMapGTable = streamsBuilder
            .globalTable(Topics.NODEID_HOST_MAP,
                    Consumed.with(Serdes.String(), Serdes.String()));

        adjGTable = streamsBuilder
            .globalTable(Topics.NODE_ADJACENT_LIST,
                    Consumed.with(Serdes.String(), new NodeAdjacentSerde()));
    }

    private void createNodeAdjLists(){
        KStream<String, NodeAdjacent> mappedAdjNodes = nodeStream
            .map((k, node) ->
                    KeyValue.pair(node.getUniqueId(), new NodeAdjacent(node, Collections.<String>emptyList())));

        mappedAdjNodes
            .to(Topics.NODE_ADJACENT_LIST, Produced.with(Serdes.String(), new NodeAdjacentSerde()));
    }


    private void matchAlertWithNodeByIp(){
        KStream<Alert, Alert> alertKeyStream = alertStream
                .selectKey((k, alert) -> alert);
        alertKeyStream
                .join(
                        ipNodeMapGTable,
                        (k, v) -> ((Alert) v).getResource().getIpAddress(),
                        (k, alert, nodeId) -> nodeId
                )
                .join(
                        adjGTable,
                        (k, nodeId) -> nodeId,
                        (k, alert, adj) -> adj.getRootNode()
                )
                .to(Topics.ALERT_NODE_MAP, Produced.with(new AlertSerde(), new NodeSerde()));

    }
    private void matchAlertWithNodeByHost(){
        KStream<Alert, Alert> alertKeyStream = alertStream
                .selectKey((k, alert) -> alert);

        alertKeyStream
                .join(
                        hostNodeMapGTable,
                        (k, v) -> ((Alert) v).getResource().getHostName(),
                        (k, alert, nodeId) -> nodeId
                )
                .join(
                        adjGTable,
                        (k, nodeId) -> nodeId,
                        (k, alert, adj) -> adj.getRootNode()
                )
                .to(Topics.ALERT_NODE_MAP, Produced.with(new AlertSerde(), new NodeSerde()));
    }

    private void printAlertNodeMap(){
        // Alert Node Data
        KStream<Alert, Node> alertNodeStream = streamsBuilder
                .stream(Topics.ALERT_NODE_MAP, Consumed.with(new AlertSerde(), new NodeSerde()));

        alertNodeStream
                .foreach((alert, node)-> System.out.println("ALERT-NODE: "+alert.getUniqueId() + "-->" + node.getUniqueId()));
    }

    private void build(){
        final KafkaStreams kafkaStreams = KafkaStreamFactory.create(streamsBuilder, "streams-watsonaiops-master");
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        kafkaStreams.setUncaughtExceptionHandler(throwable -> {
            throwable.printStackTrace();
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        });

        Runtime.getRuntime().addShutdownHook(new Thread("watsonaiops-shutdown-hook-m"){
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
