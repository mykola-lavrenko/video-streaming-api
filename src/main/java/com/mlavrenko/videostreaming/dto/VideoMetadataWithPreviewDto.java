package com.mlavrenko.videostreaming.dto;

import java.io.Serializable;

public record VideoMetadataWithPreviewDto(VideoMetadataViewDto metadata, String preview) implements Serializable {
}
