package com.glaciation.TradeOffService.model;

public class NodeResource extends Resource {
    private Long max;
    private Long available;

    public NodeResource(String name, String unit) {
        super(name, unit);
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getAvailable() {
        return available;
    }

    public void setAvailable(Long available) {
        this.available = available;
    }
}
