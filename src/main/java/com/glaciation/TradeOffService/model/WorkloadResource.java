package com.glaciation.TradeOffService.model;

public class WorkloadResource extends Resource {
    private Long demanded;
    private Long allocated;
    private Long used;

    public WorkloadResource(String name, String unit) {
        super(name, unit);
    }

    public Long getDemanded() {
        return demanded;
    }

    public void setDemanded(Long demanded) {
        this.demanded = demanded;
    }

    public Long getAllocated() {
        return allocated;
    }

    public void setAllocated(Long allocated) {
        this.allocated = allocated;
    }

    public Long getUsed() {
        return used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }
}
