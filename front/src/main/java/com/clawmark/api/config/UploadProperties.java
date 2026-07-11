package com.clawmark.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    private String localDir = "uploads";

    private String publicPathPrefix = "/uploads";

    private String publicBaseUrl = "http://localhost:8080";
}
