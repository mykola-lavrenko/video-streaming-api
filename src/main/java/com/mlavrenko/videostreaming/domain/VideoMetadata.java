package com.mlavrenko.videostreaming.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.SoftDelete;

import java.time.Duration;

/**
 * All video metadata is persisted in the same table, that's done for simplicity.
 * However, usually video streaming services have a high load, so for performance reasons this table might be split to have
 * engagement statistics in a separate table, so frequent writes to it won't block frequent reads of relatively unchangeable video metadata.
 */
@Data
@Entity
@SoftDelete
public class VideoMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String synopsis;
    private String director;
    private String castMembers;
    private int yearOfRelease;
    @Enumerated(EnumType.STRING)
    private Genre genre;
    private Duration runningTime;
    private String videoLocation;
    private int impressions;
    private int views;

    public void incrementImpressions() {
        impressions++;
    }

    public void incrementViews() {
        views++;
    }
}
