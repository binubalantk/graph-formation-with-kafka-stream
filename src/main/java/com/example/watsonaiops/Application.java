package com.example.watsonaiops;

public class Application {
    public static void main(String[] args) throws InterruptedException {
        // bootstrap the application
        System.out.println("WatsonAIOps starting up...");
        Thread t1 = new Thread(() -> {
            new MasterProcessor()
                    .initialize();
        });
        t1.start();

        Thread t2 = new Thread(() -> {
            new EdgeResolveProcessor()
                    .initialize();
        });
        t2.start();

        t1.join();
        t2.join();


    }
}
//{"uniqueId":"node1","name":"string","entityTypes":["string"],"resource":{"hostname":"host1","ipaddress":"ip1"}}
//{"uniqueIdTo":"node2","uniqueIdFrom":"node1","type":"runsOn"}
//{"uniqueId":"alert1","state":"active","summary":"string","resource":{"hostname":"host1","ipaddress":"ip1"}}
