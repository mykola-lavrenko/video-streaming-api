package com.mlavrenko.videostreaming.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for handling operations related to video content storage.
 * This interface defines methods to upload, delete, and retrieve video files in the storage system.
 * Content always represented as a string that acts as a mock to the actual video content as suggested per task specification.
 * Introduced cause there is a room for replacement by other storage alternatives, e.g. AWS S3.
 */
public interface VideoContentStorageService {
    String load(String videoLocation) throws IOException;
    String loadPreview(String videoLocation) throws IOException;
    String upload(MultipartFile videoFile) throws IOException;
    void delete(String videoLocation) throws IOException;
}
