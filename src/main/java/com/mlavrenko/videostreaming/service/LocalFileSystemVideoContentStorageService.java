package com.mlavrenko.videostreaming.service;

import com.mlavrenko.videostreaming.config.VideoContentStorageProperties;
import com.mlavrenko.videostreaming.exception.InvalidFileUploadException;
import com.mlavrenko.videostreaming.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LocalFileSystemVideoContentStorageService implements VideoContentStorageService {
    private final VideoContentStorageProperties videoContentStorageProperties;

    @Override
    public String load(String videoLocation) throws IOException {
        Path path = validateAndResolvePath(videoLocation);
        return Files.readString(path);
    }

    private static Path validateAndResolvePath(String videoLocation) {
        Path path = Path.of(videoLocation);
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Video file not found at the specified path: " + videoLocation);
        }
        return path;
    }

    @Override
    public String loadPreview(String videoLocation) throws IOException {
        Path path = validateAndResolvePath(videoLocation);
        int previewBufferSize = Math.toIntExact(videoContentStorageProperties.previewSize().toBytes());

        try (FileInputStream inputStream = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[previewBufferSize];
            int bytesRead = inputStream.read(buffer);
            return bytesRead == -1 ? StringUtils.EMPTY : new String(buffer, 0, bytesRead);
        }
    }

    @Override
    public String upload(MultipartFile videoFile) throws IOException {
        try {
            String originalFilename = Objects.requireNonNull(videoFile.getOriginalFilename(), "File name must not be null");
            Path storagePath = Path.of(videoContentStorageProperties.location(), originalFilename);
            Files.createDirectories(storagePath.getParent());
            Files.write(storagePath, videoFile.getBytes());

            return storagePath.toString();
        } catch (MalformedInputException | EOFException | NullPointerException e) {
            throw new InvalidFileUploadException("Failed to upload an invalid file:", e);
        }
    }

    @Override
    public void delete(String videoLocation) throws IOException {
        Files.deleteIfExists(Path.of(videoLocation));
    }
}
