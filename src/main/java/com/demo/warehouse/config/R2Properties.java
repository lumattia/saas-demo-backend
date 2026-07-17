package com.demo.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "r2")
public class R2Properties {
    private String accessKeyId;
    private String secretAccessKey;
    private String endpoint;
    private String bucketName;
    private String publicUrl;

    // getters and setters ...
}