package com.glaciation.TradeOffService.configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class TradeOffConfiguration {

    @Value(value = "${influxdb.url}")
    private String influxDBUrl;
    @Value(value = "${influxdb.username}")
    private String influxDBUsername;
    @Value(value = "${influxdb.password}")
    private String influxDBPassword;
    @Value(value = "${influxdb.token}")
    private String influxDBToken;
    @Value(value = "${influxdb.organization}")
    private String influxDBOrganization;
    @Value(value = "${influxdb.bucket}")
    private String influxDBBucket;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(influxDBUrl, influxDBToken.toCharArray(), influxDBOrganization, influxDBBucket);
    }

    public String getInfluxDBUrl() {
        return influxDBUrl;
    }

    public String getInfluxDBUsername() {
        return influxDBUsername;
    }

    public String getInfluxDBPassword() {
        return influxDBPassword;
    }

    public String getInfluxDBToken() {
        return influxDBToken;
    }

    public String getInfluxDBOrganization() {
        return influxDBOrganization;
    }

    public String getInfluxDBBucket() {
        return influxDBBucket;
    }
}
