package com.glaciation.TradeOffService.model;

public class ResourceMapping {
    String key;
    String promql;
    String dkgName;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPromql() {
        return promql;
    }

    public void setPromql(String promql) {
        this.promql = promql;
    }

    public String getDkgName() {
        return dkgName;
    }

    public void setDkgName(String dkgName) {
        this.dkgName = dkgName;
    }
}
