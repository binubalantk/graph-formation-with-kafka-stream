package com.example.watsonaiops.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Alert{
    /**
     * The unique id for an Alert
     */
    @JsonProperty("uniqueId")
    private String uniqueId;

    /**
     * The Alert state
     */
    @JsonProperty("state")
    private String state;

    /**
     * The Alert Summary
     */
    @JsonProperty("summary")
    private String summary;

    /**
     * The Alert Resource
     */
    @JsonProperty("resource")
    private Resource resource;

    public Alert() {
    }

    public Alert(String uniqueId, String state, String summary, Resource resource) {
        this.uniqueId = uniqueId;
        this.state = state;
        this.summary = summary;
        this.resource = resource;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
