package com.mlavrenko.videostreaming.controller;

import com.mlavrenko.videostreaming.dto.EngagementStatisticsDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataViewDto;
import com.mlavrenko.videostreaming.dto.VideoMetadataWithPreviewDto;
import com.mlavrenko.videostreaming.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(value = "/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping
    @ResponseStatus(CREATED)
    public VideoMetadataDto publish(@RequestPart("metadata") @Valid VideoMetadataDto metadataDto,
                                    @RequestPart("videoFile") MultipartFile videoFile) throws IOException {
        return videoService.publishVideo(metadataDto, videoFile);
    }

    @PutMapping("/{id}")
    public VideoMetadataDto update(@PathVariable long id, @RequestBody @Valid VideoMetadataDto metadataDto) {
        return videoService.updateMetadata(id, metadataDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delist(@PathVariable("id") long id) {
        videoService.softDeleteVideo(id);
    }

    @GetMapping("/{id}")
    public VideoMetadataWithPreviewDto load(@PathVariable("id") long id) throws IOException {
        return videoService.loadVideo(id);
    }

    @GetMapping("/{id}/play")
    public String play(@PathVariable long id) throws IOException {
        return videoService.playVideo(id);
    }

    @GetMapping
    public Page<VideoMetadataViewDto> listAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) Integer yearOfRelease,
            @PageableDefault Pageable pageable) {
        return videoService.listAllVideos(title, director, yearOfRelease, pageable);
    }

    @GetMapping("/{id}/engagement-statistics")
    public EngagementStatisticsDto getEngagementStatistics(@PathVariable("id") long id) {
        return videoService.getEngagementStatistics(id);
    }
}
