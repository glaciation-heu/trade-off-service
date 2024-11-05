package com.glaciation.TradeOffService.model;

public class ResourceMapping {
    String key;
    ResourcePromql promql;
    String dkgName;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ResourcePromql getPromql() {
        return promql;
    }

    public void setPromql(ResourcePromql promql) {
        this.promql = promql;
    }

    public String getDkgName() {
        return dkgName;
    }

    public void setDkgName(String dkgName) {
        this.dkgName = dkgName;
    }
}
