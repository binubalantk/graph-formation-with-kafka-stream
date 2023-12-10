package com.example.watsonaiops.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class NodeAdjacent {
    /**
     * The root (or From) Node
     */
    @JsonProperty("rootNode")
    private Node rootNode;

    /**
     * The list of Adjacent Nodes
     */
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

    /**
     * Method to add a new node to the adjacent list, if not already exists
     * @param toNodeId The 'To' Node Id
     */
    public void addAdjNodeIfNotExists(String toNodeId){
        if(adjNodes.contains(toNodeId)){
            return;
        }
        adjNodes.add(toNodeId);
    }
}
