package com.mlavrenko.videostreaming.service;

import com.mlavrenko.videostreaming.IntegrationTest;
import com.mlavrenko.videostreaming.domain.Genre;
import com.mlavrenko.videostreaming.domain.VideoMetadata;
import com.mlavrenko.videostreaming.dto.EngagementStatisticsDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataViewDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataWithPreviewDto;
import com.mlavrenko.videostreaming.exception.ResourceNotFoundException;
import com.mlavrenko.videostreaming.repository.VideoMetadataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@IntegrationTest
class VideoServiceTest {
    @Autowired
    private VideoService videoService;
    @MockitoBean
    private VideoMetadataRepository videoMetadataRepository;
    @MockitoBean
    private VideoContentStorageService videoContentStorageService;
    @MockitoBean
    private VideoSearchSpecification videoSearchSpecification;

    @Test
    void testPublishVideoWhenInputValidThenSuccessfulUpload() throws IOException {
        VideoMetadataDto videoMetadataDto = createSampleVideoDto();
        MultipartFile videoFile = Mockito.mock(MultipartFile.class);

        String fakeLocation = "fake-location/video.mp4";
        when(videoContentStorageService.upload(videoFile)).thenReturn(fakeLocation);
        when(videoMetadataRepository.save(any(VideoMetadata.class))).thenAnswer(invocation -> {
            VideoMetadata videoMetadata = invocation.getArgument(0);
            videoMetadata.setId(1L);
            return videoMetadata;
        });

        VideoMetadataDto result = videoService.publishVideo(videoMetadataDto, videoFile);

        assertAll(
                () -> assertThat(result).isNotNull().usingRecursiveComparison().ignoringFields("id").isEqualTo(videoMetadataDto),
                () -> verify(videoContentStorageService).upload(videoFile),
                () -> verify(videoMetadataRepository).save(any(VideoMetadata.class))
        );
    }

    @Test
    void testPublishVideoWhenUploadFailsThenNothingPersisted() throws IOException {
        VideoMetadataDto videoMetadataDto = createSampleVideoDto();
        MultipartFile videoFile = Mockito.mock(MultipartFile.class);
        String message = "Upload failed";
        when(videoContentStorageService.upload(videoFile)).thenThrow(new IOException(message));

        assertAll(
                () -> assertThatThrownBy(() -> videoService.publishVideo(videoMetadataDto, videoFile)).isInstanceOf(IOException.class).hasMessage(message),
                () -> verify(videoContentStorageService).upload(videoFile),
                () -> verifyNoMoreInteractions(videoMetadataRepository)
        );
    }

    @Test
    void testPublishVideoWhenSaveFailsThenCleanupTriggered() throws IOException {
        VideoMetadataDto videoMetadataDto = createSampleVideoDto();
        MultipartFile videoFile = Mockito.mock(MultipartFile.class);

        String fakeLocation = "fake-location/video.mp4";
        when(videoContentStorageService.upload(videoFile)).thenReturn(fakeLocation);
        String message = "Database save error";
        when(videoMetadataRepository.save(any(VideoMetadata.class))).thenThrow(new RuntimeException(message));

        assertAll(
                () -> assertThatThrownBy(() -> videoService.publishVideo(videoMetadataDto, videoFile)).isInstanceOf(RuntimeException.class).hasMessageContaining(message),
                () -> verify(videoContentStorageService).upload(videoFile),
                () -> verify(videoContentStorageService).delete(fakeLocation),
                () -> verify(videoMetadataRepository).save(any(VideoMetadata.class))
        );
    }

    @Test
    void testUpdateMetadataWhenValidInputThenSuccessfulUpdate() {
        long videoId = 1L;
        VideoMetadataDto metadataDto = createSampleVideoDto();

        VideoMetadata existingVideo = new VideoMetadata();
        existingVideo.setId(videoId);

        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.of(existingVideo));
        when(videoMetadataRepository.save(any(VideoMetadata.class))).thenReturn(existingVideo);

        VideoMetadataDto result = videoService.updateMetadata(videoId, metadataDto);

        assertThat(result).isNotNull().usingRecursiveComparison().ignoringFields("id").isEqualTo(existingVideo);
    }

    @Test
    void testUpdateMetadataWhenVideoMetadataNotFoundThenThrowsNotFoundException() {
        long videoId = 1L;
        VideoMetadataDto metadataDto = createSampleVideoDto();

        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.updateMetadata(videoId, metadataDto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testSoftDeleteVideoWhenVideoMetadataExistsThenDeletesIt() {
        long videoId = 1L;
        when(videoMetadataRepository.existsById(videoId)).thenReturn(true);

        videoService.softDeleteVideo(videoId);

        verify(videoMetadataRepository).deleteById(videoId);
    }

    @Test
    void testSoftDeleteVideoWhenVideoMetadataNotFoundThenThrowsNotFoundException() {
        long videoId = 1L;
        when(videoMetadataRepository.existsById(videoId)).thenReturn(false);

        assertThatThrownBy(() -> videoService.softDeleteVideo(videoId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testLoadVideoWhenVideoMetadataExistsThenReturnsVideoMetadataAndPreviewContent() throws IOException {
        long videoId = 1L;

        VideoMetadata video = new VideoMetadata();
        video.setId(videoId);
        video.setImpressions(5);

        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.of(video));

        VideoMetadataWithPreviewDto result = videoService.loadVideo(videoId);

        assertAll(
                () -> assertThat(result).isNotNull().extracting("metadata.id").isEqualTo(videoId),
                () -> verify(videoMetadataRepository).findById(videoId)
        );
    }

    @Test
    void testLoadVideoWhenVideoMetadataNotFoundThenThrowsNotFoundException() {
        long videoId = 1L;
        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.loadVideo(videoId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testPlayVideoWhenVideoExistsThenReturnVideoContent() throws IOException {
        long videoId = 1L;
        String videoLocation = "fake-location/video.mp4";

        VideoMetadata video = new VideoMetadata();
        video.setId(videoId);
        video.setVideoLocation(videoLocation);

        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.of(video));
        String videoContent = "Video Content";
        when(videoContentStorageService.load(videoLocation)).thenReturn(videoContent);

        String result = videoService.playVideo(videoId);

        assertThat(result).isEqualTo(videoContent);
    }

    @Test
    void testPlayVideoWhenVideoNotFoundThenThrowNotFoundException() {
        long videoId = 1L;
        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.playVideo(videoId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testListAllVideos() {
        String title = "Title";
        String director = "Director";
        int yearOfRelease = 2023;
        PageRequest pageable = PageRequest.of(0, 10);

        VideoMetadataViewDto videoMetadataViewDto = new VideoMetadataViewDto(null, title, director, "Stepan Hiha", Genre.ACTION, Duration.ZERO);
        Page<VideoMetadataViewDto> metadataPage = new PageImpl<>(List.of(videoMetadataViewDto));

        Specification<VideoMetadata> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        when(videoSearchSpecification.createSearchSpecification(title, director, yearOfRelease)).thenReturn(specification);
        VideoMetadata videoMetadata = new VideoMetadata();
        BeanUtils.copyProperties(videoMetadataViewDto, videoMetadata);
        when(videoMetadataRepository.findAll(specification, pageable)).thenReturn(metadataPage.map(metadata -> videoMetadata));

        Page<VideoMetadataViewDto> result = videoService.listAllVideos(title, director, yearOfRelease, pageable);

        assertThat(result).isNotNull().hasSize(1);
    }

    @Test
    void testGetEngagementStatisticsWhenVideoMetadataExistsThenReturnEngagementStatistics() {
        long videoId = 1L;

        VideoMetadata video = new VideoMetadata();
        video.setId(videoId);
        video.setImpressions(100);
        video.setViews(50);

        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.of(video));

        EngagementStatisticsDto stats = videoService.getEngagementStatistics(videoId);

        assertThat(stats).isEqualTo(new EngagementStatisticsDto(video.getImpressions(), video.getViews()));
    }

    @Test
    void testGetEngagementStatisticsWhenNotFoundThenReturnNotFoundException() {
        long videoId = 1L;
        when(videoMetadataRepository.findById(videoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.getEngagementStatistics(videoId)).isInstanceOf(ResourceNotFoundException.class);
    }

    private VideoMetadataDto createSampleVideoDto() {
        return new VideoMetadataDto(
                null,
                "Test Video",
                "Test Synopsis",
                "Test Director",
                "Actor A, Actor B",
                2023,
                Genre.ACTION,
                Duration.ofMinutes(90)
        );
    }
}