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
    /**
     * The StreamBuilder Object
     */
    StreamsBuilder streamsBuilder = new StreamsBuilder();

    /**
     * Kafka KStream for Nodes
     */
    KStream<String, Node> nodeStream;

    /**
     * Kafka KStream for Alerts
     */
    KStream<String, Alert> alertStream;

    /**
     * Kafka GlobalKTable for IP-Node map
     */
    GlobalKTable<String, String> ipNodeMapGTable;

    /**
     * Kafka GlobalKTable for Hostname-Node map
     */
    GlobalKTable<String, String> hostNodeMapGTable;

    /**
     * Kafka GlobalKTable for Node Adjacent list
     */
    GlobalKTable<String, NodeAdjacent> adjGTable;

    /**
     * Method to initialize the Master processor topology
     */
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

    /**
     * Method to load Node stream
     */
    private void loadNodeStream(){
        // node
        nodeStream = streamsBuilder
            .stream(Topics.NODE_TOPOLOGICAL_DATA, Consumed.with(Serdes.String(), new NodeSerde()));

        nodeStream
            .foreach((k, v)-> System.out.println("NODE: "+v.getUniqueId()));
    }

    /**
     * Method to load Alert stream
     */
    private void loadAlerts(){
        alertStream = streamsBuilder
            .stream(Topics.ALERT, Consumed.with(Serdes.String(), new AlertSerde()));

        alertStream
            .foreach((k, v)-> System.out.println("ALERT: "+v.getUniqueId()));
    }

    /**
     * Method to generate IP-Node maps
     */
    private void mapIpToNodeId(){
        // node to ip address map
        nodeStream
            .map((k, node) -> KeyValue.pair(node.getResource().getIpAddress(), node.getUniqueId()))
            .to(Topics.NODEID_IP_MAP, Produced.with(Serdes.String(), Serdes.String()));
    }

    /**
     * Method to generate Hostname-Node maps
     */
    private void mapHostToNodeId(){
        // node to hostname map
        nodeStream
            .map((k, node) -> KeyValue.pair(node.getResource().getHostName(), node.getUniqueId()))
            .to(Topics.NODEID_HOST_MAP, Produced.with(Serdes.String(), Serdes.String()));
    }

    /**
     * Method to load GlobalKTables for IP-Node map, Hostname-Node map and Node Adjacent list
     */
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

    /**
     * Method to create new Node Adjacent list for a newly added Node
     */
    private void createNodeAdjLists(){
        KStream<String, NodeAdjacent> mappedAdjNodes = nodeStream
            .map((k, node) ->
                    KeyValue.pair(node.getUniqueId(), new NodeAdjacent(node, Collections.<String>emptyList())));

        mappedAdjNodes
            .to(Topics.NODE_ADJACENT_LIST, Produced.with(Serdes.String(), new NodeAdjacentSerde()));
    }


    /**
     * Method to match Alert resource IPs with Node Resource IPs
     */
    private void matchAlertWithNodeByIp(){
        KStream<Alert, Alert> alertKeyStream = alertStream
                .selectKey((k, alert) -> alert);
        alertKeyStream
                .join(
                        ipNodeMapGTable,
                        (k, v) -> v.getResource().getIpAddress(),
                        (k, alert, nodeId) -> nodeId
                )
                .join(
                        adjGTable,
                        (k, nodeId) -> nodeId,
                        (k, alert, adj) -> adj.getRootNode()
                )
                .to(Topics.ALERT_NODE_MAP, Produced.with(new AlertSerde(), new NodeSerde()));

    }

    /**
     * Method to match Alert resource Hostnames with Node Resource Hostnames
     */
    private void matchAlertWithNodeByHost(){
        KStream<Alert, Alert> alertKeyStream = alertStream
                .selectKey((k, alert) -> alert);

        alertKeyStream
                .join(
                        hostNodeMapGTable,
                        (k, v) -> v.getResource().getHostName(),
                        (k, alert, nodeId) -> nodeId
                )
                .join(
                        adjGTable,
                        (k, nodeId) -> nodeId,
                        (k, alert, adj) -> adj.getRootNode()
                )
                .to(Topics.ALERT_NODE_MAP, Produced.with(new AlertSerde(), new NodeSerde()));
    }

    /**
     * Method to print Alert-Node map result
     */
    private void printAlertNodeMap(){
        // Alert Node Data
        KStream<Alert, Node> alertNodeStream = streamsBuilder
                .stream(Topics.ALERT_NODE_MAP, Consumed.with(new AlertSerde(), new NodeSerde()));

        alertNodeStream
                .foreach((alert, node)-> System.out.println("ALERT-NODE: "+alert.getUniqueId() + "-->" + node.getUniqueId()));
    }

    /**
     * Method to build the Kafka Stream Processor Topology
     */
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
