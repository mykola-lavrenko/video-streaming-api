package com.mlavrenko.videostreaming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlavrenko.videostreaming.IntegrationTest;
import com.mlavrenko.videostreaming.domain.Genre;
import com.mlavrenko.videostreaming.domain.VideoMetadata;
import com.mlavrenko.videostreaming.dto.VideoMetadataDto;
import com.mlavrenko.videostreaming.repository.VideoMetadataRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class VideoControllerTest {
    private static final String API_PATH = "/api/v1/videos";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private VideoMetadataRepository videoMetadataRepository;

    @AfterAll
    static void cleanUp() throws IOException {
        Files.deleteIfExists(Path.of("uploads"));
    }

    @Test
    void testPublishShouldPersistObjectAndStoreFileWhenRequestIsValid() throws Exception {
        VideoMetadataDto videoMetadataDto = getVideoMetadataDto();

        MockMultipartFile metadataFile = new MockMultipartFile(
                "metadata",
                "metadata.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(videoMetadataDto)
        );

        String videoContent = "Dummy video content";
        MockMultipartFile videoFile = new MockMultipartFile(
                "videoFile",
                "video.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                videoContent.getBytes()
        );

        mockMvc.perform(multipart(API_PATH)
                        .file(metadataFile)
                        .file(videoFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(1),
                        jsonPath("$.title").value(videoMetadataDto.title()),
                        jsonPath("$.director").value(videoMetadataDto.director()),
                        jsonPath("$.yearOfRelease").value(videoMetadataDto.yearOfRelease())
                );

        assertThat(videoMetadataRepository.findAll())
                .hasSize(1)
                .first()
                .usingRecursiveComparison()
                .comparingOnlyFields("title", "director", "yearOfRelease", "castMembers", "genre", "runningTime")
                .isEqualTo(videoMetadataDto);

        File storedFile = new File("uploads/video.mp4");
        assertThat(storedFile).exists();
        assertThat(Files.readString(storedFile.toPath())).isEqualTo(videoContent);

        Files.deleteIfExists(storedFile.toPath());
    }

    @Test
    void testPublishShouldNotPersistObjectOrStoreFileWhenMetadataIsInvalid() throws Exception {
        MockMultipartFile metadataFile = new MockMultipartFile(
                "metadata",
                "metadata.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{}".getBytes()
        );

        MockMultipartFile videoFile = new MockMultipartFile(
                "videoFile",
                "video.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Dummy video content".getBytes()
        );

        mockMvc.perform(multipart(API_PATH)
                        .file(metadataFile)
                        .file(videoFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        assertThat(videoMetadataRepository.count()).isZero();
        assertThat(new File("uploads/video.mp4")).doesNotExist();
    }

    @Test
    void testPublishVideoShouldNotPersistObjectOrStoreFileWhenFileIsInvalid() throws Exception {
        VideoMetadataDto videoMetadataDto = getVideoMetadataDto();

        MockMultipartFile metadataFile = new MockMultipartFile(
                "metadata",
                "metadata.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(videoMetadataDto)
        );
        MockMultipartFile videoFile = Mockito.spy(new MockMultipartFile(
                "videoFile",
                "video.mp4",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[0]
        ));

        doThrow(new EOFException("Mocked EOFException")).when(videoFile).getBytes();

        mockMvc.perform(multipart(API_PATH)
                        .file(metadataFile)
                        .file(videoFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        assertThat(videoMetadataRepository.count()).isZero();
        assertThat(new File("uploads/video.mp4")).doesNotExist();
    }

    @Test
    void testLoadShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/videos/999")).andExpect(status().isNotFound());
    }

    @Test
    void testPlayShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/videos/999/play")).andExpect(status().isNotFound());
    }

    @Test
    void testGetEngagementStatisticsShouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/videos/999/engagement-statistics")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({
            "Video A, , ",
            ", Director B, ",
            ", , 2022",
            "Video A, Director B, 2022"
    })
    void testListAllParameterized(String title, String director, Integer yearOfRelease) throws Exception {
        VideoMetadata firstVideo = new VideoMetadata();
        firstVideo.setTitle("Video A");
        firstVideo.setDirector("Director A");
        firstVideo.setYearOfRelease(2022);

        VideoMetadata secondVideo = new VideoMetadata();
        secondVideo.setTitle("Video B");
        secondVideo.setDirector("Director B");
        secondVideo.setYearOfRelease(2023);

        videoMetadataRepository.save(firstVideo);
        videoMetadataRepository.save(secondVideo);

        mockMvc.perform(get(API_PATH)
                        .param("title", title == null ? "" : title)
                        .param("director", director == null ? "" : director)
                        .param("yearOfRelease", yearOfRelease == null ? "" : yearOfRelease.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private static VideoMetadataDto getVideoMetadataDto() {
        return new VideoMetadataDto(
                null,
                "Test Video",
                "Test Synopsis",
                "Test Director",
                "Brad Pitt",
                2023,
                Genre.ACTION,
                Duration.ZERO
        );
    }
}
