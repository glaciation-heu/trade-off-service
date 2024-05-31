package com.glaciation.TradeOffService.model;

import java.util.Date;

public class RunInfo {
    private String nodeId;
    private Date startTime;
    private Date endTime;

    public RunInfo(String nodeId, Date startTime, Date endTime) {
        this.nodeId = nodeId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
