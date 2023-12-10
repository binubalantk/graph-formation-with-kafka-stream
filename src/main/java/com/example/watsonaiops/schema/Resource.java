package com.example.watsonaiops.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Resource {
    /**
     * The Host name
     */
    @JsonProperty("hostname")
    private String hostName;

    /**
     * The IP Address
     */
    @JsonProperty("ipaddress")
    private String ipAddress;

    public Resource() {
    }

    public Resource(String hostName, String ipAddress) {
        this.hostName = hostName;
        this.ipAddress = ipAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
