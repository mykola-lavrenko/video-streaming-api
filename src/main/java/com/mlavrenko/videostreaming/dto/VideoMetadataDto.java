package com.mlavrenko.videostreaming.dto;

import com.mlavrenko.videostreaming.domain.Genre;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.time.Duration;

public record VideoMetadataDto(
        Long id,
        @NotBlank
        String title,
        @NotBlank
        String synopsis,
        @NotBlank
        String director,
        @NotBlank
        String castMembers,
        @Positive
        int yearOfRelease,
        @Enumerated(EnumType.STRING)
        Genre genre,
        Duration runningTime) implements Serializable {
}
