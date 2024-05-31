package com.glaciation.TradeOffService.model;

import java.util.List;

public class Node {
    private String id;
    private List<NodeResource> resources;

    public Node(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<NodeResource> getResources() {
        return resources;
    }

    public void setResources(List<NodeResource> resources) {
        this.resources = resources;
    }
}
