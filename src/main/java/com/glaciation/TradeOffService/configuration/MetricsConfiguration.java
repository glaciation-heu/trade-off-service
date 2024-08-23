package com.glaciation.TradeOffService.configuration;

import com.glaciation.TradeOffService.model.NodeMetric;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "metrics")
public class MetricsConfiguration {
    String metricNameKey;
    String metricValueKey;
    String metricUnitKey;
    String nodeIdKey;
    String workloadIdKey;
    List<NodeMetric> nodes;
    List<NodeMetric> workloads;

    public String getMetricUnitKey() {
        return metricUnitKey;
    }

    public void setMetricUnitKey(String metricUnitKey) {
        this.metricUnitKey = metricUnitKey;
    }

    public String getMetricNameKey() {
        return metricNameKey;
    }

    public void setMetricNameKey(String metricNameKey) {
        this.metricNameKey = metricNameKey;
    }

    public String getMetricValueKey() {
        return metricValueKey;
    }

    public void setMetricValueKey(String metricValueKey) {
        this.metricValueKey = metricValueKey;
    }

    public String getNodeIdKey() {
        return nodeIdKey;
    }

    public void setNodeIdKey(String nodeIdKey) {
        this.nodeIdKey = nodeIdKey;
    }

    public String getWorkloadIdKey() {
        return workloadIdKey;
    }

    public void setWorkloadIdKey(String workloadIdKey) {
        this.workloadIdKey = workloadIdKey;
    }

    public List<NodeMetric> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeMetric> nodes) {
        this.nodes = nodes;
    }

    public List<NodeMetric> getWorkloads() {
        return workloads;
    }

    public void setWorkloads(List<NodeMetric> workloads) {
        this.workloads = workloads;
    }
}
