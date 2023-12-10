package com.example.watsonaiops.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Edge{
    /**
     * The Unique id of the 'To' Node
     */
    @JsonProperty("uniqueIdTo")
    private String uniqueIdTo;

    /**
     * The Unique id of the 'From' Node
     */
    @JsonProperty("uniqueIdFrom")
    private String uniqueIdFrom;

    /**
     * Edge type
     */
    @JsonProperty("type")
    private String type;

    public Edge() {
    }

    public Edge(String uniqueIdTo, String uniqueIdFrom, String type) {
        this.uniqueIdTo = uniqueIdTo;
        this.uniqueIdFrom = uniqueIdFrom;
        this.type = type;
    }

    public String getUniqueIdTo() {
        return uniqueIdTo;
    }

    public void setUniqueIdTo(String uniqueIdTo) {
        this.uniqueIdTo = uniqueIdTo;
    }

    public String getUniqueIdFrom() {
        return uniqueIdFrom;
    }

    public void setUniqueIdFrom(String uniqueIdFrom) {
        this.uniqueIdFrom = uniqueIdFrom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
