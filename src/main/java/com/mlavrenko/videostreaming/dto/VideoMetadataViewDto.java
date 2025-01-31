package com.mlavrenko.videostreaming.dto;

import com.mlavrenko.videostreaming.domain.Genre;

import java.io.Serializable;
import java.time.Duration;

public record VideoMetadataViewDto(
        Long id,
        String title,
        String director,
        String mainActor,
        Genre genre,
        Duration runningTime) implements Serializable {
}
