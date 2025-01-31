package com.mlavrenko.videostreaming.dto;

import java.io.Serializable;

public record EngagementStatisticsDto(long impressions, long views) implements Serializable {
}
