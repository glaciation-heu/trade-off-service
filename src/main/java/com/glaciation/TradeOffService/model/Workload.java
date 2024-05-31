package com.glaciation.TradeOffService.model;

import java.util.List;

public class Workload {
    private String id;
    private RunInfo runsOn;
    private List<WorkloadResource> resources;

    public Workload(String id, RunInfo runsOn) {
        this.id = id;
        this.runsOn = runsOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<WorkloadResource> getResources() {
        return resources;
    }

    public void setResources(List<WorkloadResource> resources) {
        this.resources = resources;
    }

    public RunInfo getRunsOn() {
        return runsOn;
    }

    public void setRunsOn(RunInfo runsOn) {
        this.runsOn = runsOn;
    }
}

