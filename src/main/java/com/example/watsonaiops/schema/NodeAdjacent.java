package com.example.watsonaiops.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class NodeAdjacent {
    @JsonProperty("rootNode")
    private Node rootNode;

    @JsonProperty("adjNodes")
    private List<String> adjNodes = Collections.emptyList();

    public NodeAdjacent() {
    }

    public NodeAdjacent(Node rootNode, List<String> adjNodes) {
        this.rootNode = rootNode;
        this.adjNodes = adjNodes;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public List<String> getAdjNodes() {
        return adjNodes;
    }

    public void setAdjNodes(List<String> adjNodes) {
        this.adjNodes = adjNodes;
    }

    public void addAdjNodeIfNotExists(String toNodeId){
        if(adjNodes.contains(toNodeId)){
            return;
        }
        adjNodes.add(toNodeId);
    }
}
