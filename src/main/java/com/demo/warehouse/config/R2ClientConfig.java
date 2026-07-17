package com.demo.warehouse.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import java.net.URI;

@Configuration
public class R2ClientConfig {

    private final R2Properties r2Properties;

    public R2ClientConfig(R2Properties r2Properties) {
        this.r2Properties = r2Properties;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1) // R2 忽略区域，但 SDK 需要填一个
                .endpointOverride(URI.create(r2Properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                r2Properties.getAccessKeyId(),
                                r2Properties.getSecretAccessKey())))
                .build();
    }
}