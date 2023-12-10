package com.example.watsonaiops.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Node{
    /**
     * The Unique Node Id
     */
    @JsonProperty("uniqueId")
    private String uniqueId;

    /**
     * The Node name
     */
    @JsonProperty("name")
    private String name;

    /**
     * The list of Entity types
     */
    @JsonProperty("entityTypes")
    private String[] entityTypes;

    /**
     * The Resource associated with the Node
     */
    @JsonProperty("resource")
    private Resource resource;

    public Node() {
    }

    public Node(String uniqueId, String name, String[] entityTypes, Resource resource) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.entityTypes = entityTypes;
        this.resource = resource;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(String[] entityTypes) {
        this.entityTypes = entityTypes;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
