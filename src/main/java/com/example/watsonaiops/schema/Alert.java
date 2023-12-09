package com.example.watsonaiops.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Alert  implements JsonSerdeData{
    @JsonProperty("uniqueId")
    private String uniqueId;

    @JsonProperty("state")
    private String state;

    @JsonProperty("summary")
    private String summary;

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
