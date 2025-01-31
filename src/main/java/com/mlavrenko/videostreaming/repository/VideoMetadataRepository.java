package com.mlavrenko.videostreaming.repository;

import com.mlavrenko.videostreaming.domain.VideoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoMetadataRepository extends JpaRepository<VideoMetadata, Long>, JpaSpecificationExecutor<VideoMetadata> {
}
