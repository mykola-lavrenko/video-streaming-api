package com.mlavrenko.videostreaming.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "video-content-storage")
public record VideoContentStorageProperties(String location, DataSize previewSize) {
}
