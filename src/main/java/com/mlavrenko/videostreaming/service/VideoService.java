package com.mlavrenko.videostreaming.service;

import com.mlavrenko.videostreaming.domain.VideoMetadata;
import com.mlavrenko.videostreaming.dto.EngagementStatisticsDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataViewDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataWithPreviewDto;
import com.mlavrenko.videostreaming.exception.ResourceNotFoundException;
import com.mlavrenko.videostreaming.repository.VideoMetadataRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoService {
    private final VideoMetadataRepository videoMetadataRepository;
    private final VideoContentStorageService videoContentStorageService;
    private final VideoSearchSpecification videoSearchSpecification;

    public VideoMetadataDto publishVideo(@Valid VideoMetadataDto videoMetadataDto, MultipartFile videoFile) throws IOException {
        String videoLocation = null;
        try {
            videoLocation = videoContentStorageService.upload(videoFile);
            VideoMetadata videoMetadata = toVideoMetadata(videoMetadataDto, new VideoMetadata());
            videoMetadata.setVideoLocation(videoLocation);
            return toVideoMetadataDto(videoMetadataRepository.save(videoMetadata));
        } catch (Exception e) {
            rollbackVideoUpload(videoLocation);
            throw e;
        }
    }

    public VideoMetadataDto updateMetadata(Long id, VideoMetadataDto videoMetadataDto) {
        return videoMetadataRepository.findById(id)
                .map(videoMetadata -> toVideoMetadata(videoMetadataDto, videoMetadata))
                .map(VideoService::toVideoMetadataDto)
                .orElseThrow(VideoService::createNotFoundException);
    }

    /**
     * There are two different opinions, what should be the response, in case there is an attempt to delete not existing resource:
     * 1. return resource not found exception
     * 2. proceed as if resource has been deleted, cause result is the same as for deleting of existing resource - resource does not exist
     * The first approach is chosen.
     */
    public void softDeleteVideo(Long id) {
        if (videoMetadataRepository.existsById(id)) {
            videoMetadataRepository.deleteById(id);
        } else {
            throw createNotFoundException();
        }
    }

    public VideoMetadataWithPreviewDto loadVideo(Long id) throws IOException {
        VideoMetadata video = videoMetadataRepository.findById(id).orElseThrow(VideoService::createNotFoundException);
        video.incrementImpressions();
        return new VideoMetadataWithPreviewDto(toVideoMetadataViewDto(video), videoContentStorageService.loadPreview(video.getVideoLocation()));
    }

    public String playVideo(Long id) throws IOException {
        VideoMetadata videoMetadata = videoMetadataRepository.findById(id).orElseThrow(VideoService::createNotFoundException);
        videoMetadata.incrementViews();
        return videoContentStorageService.load(videoMetadata.getVideoLocation());
    }

    /**
     * For simplicity, there is a single method with several optional parameters for a search.
     * Of course, there are might be cases when only some limited or even single field is used for filtration.
     * In this case, by following YAGNI principle, it would be better to have only required params.
     * Also, it might be a case, that filtering might be done by any field, but adding all fields as optional params
     * might reduce maintainability, so some generic search alternative might be introduced.
     */
    public Page<VideoMetadataViewDto> listAllVideos(String title, String director, Integer yearOfRelease, Pageable pageable) {
        Specification<VideoMetadata> specification = videoSearchSpecification.createSearchSpecification(title, director, yearOfRelease);
        return videoMetadataRepository.findAll(specification, pageable).map(VideoService::toVideoMetadataViewDto);
    }

    public EngagementStatisticsDto getEngagementStatistics(Long id) {
        return videoMetadataRepository.findById(id)
                .map(video -> new EngagementStatisticsDto(video.getImpressions(), video.getViews()))
                .orElseThrow(VideoService::createNotFoundException);
    }

    private void rollbackVideoUpload(String videoLocation) throws IOException {
        if (videoLocation != null) {
            videoContentStorageService.delete(videoLocation);
        }
    }

    private static ResourceNotFoundException createNotFoundException() {
        return new ResourceNotFoundException("Video not found");
    }

    private static VideoMetadata toVideoMetadata(VideoMetadataDto videoMetadataDto, VideoMetadata videoMetadata) {
        BeanUtils.copyProperties(videoMetadataDto, videoMetadata, "id");
        return videoMetadata;
    }

    private static VideoMetadataDto toVideoMetadataDto(VideoMetadata videoMetadata) {
        return new VideoMetadataDto(
                videoMetadata.getId(),
                videoMetadata.getTitle(),
                videoMetadata.getSynopsis(),
                videoMetadata.getDirector(),
                videoMetadata.getCastMembers(),
                videoMetadata.getYearOfRelease(),
                videoMetadata.getGenre(),
                videoMetadata.getRunningTime()
        );
    }

    private static VideoMetadataViewDto toVideoMetadataViewDto(VideoMetadata videoMetadata) {
        return new VideoMetadataViewDto(
                videoMetadata.getId(),
                videoMetadata.getTitle(),
                videoMetadata.getDirector(),
                getMainActor(videoMetadata.getCastMembers()),
                videoMetadata.getGenre(),
                videoMetadata.getRunningTime()
        );
    }

    /**
     * Assumption, for a simplicity, that the main actor goes first in a comma-delimited list of cast members.
     */
    private static String getMainActor(String castMembers) {
        return Optional.ofNullable(castMembers)
                .filter(s -> !s.isBlank())
                .map(StringUtils::commaDelimitedListToStringArray)
                .map(actors -> actors[0])
                .orElse(null);
    }
}
