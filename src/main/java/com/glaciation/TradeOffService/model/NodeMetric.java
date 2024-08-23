package com.glaciation.TradeOffService.model;

import java.util.List;

public class NodeMetric {
    String name;
    List<ResourceMapping> mappings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResourceMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<ResourceMapping> mappings) {
        this.mappings = mappings;
    }
}
