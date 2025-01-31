package com.mlavrenko.videostreaming.service;

import com.mlavrenko.videostreaming.config.VideoContentStorageProperties;
import com.mlavrenko.videostreaming.exception.InvalidFileUploadException;
import com.mlavrenko.videostreaming.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalFileSystemVideoContentStorageServiceTest {
    private final LocalFileSystemVideoContentStorageService service =
            new LocalFileSystemVideoContentStorageService(new VideoContentStorageProperties("uploads", DataSize.ofBytes(16)));

    @AfterAll
    static void cleanUp() throws IOException {
        Files.deleteIfExists(Path.of("uploads"));
    }

    @Nested
    class LoadPreviewTests {
        @ParameterizedTest
        @CsvSource(value = {
                "sample content for preview which is long enough, sample content f",
                "partial content, partial content",
                "empty,empty",
        }, emptyValue = "empty")
        void testLoadPreviewWhenValidPathThenReturnsPreviewContent(String text, String expected) throws IOException {
            Path tempFile = Files.createTempFile("test-preview", ".txt");
            Files.writeString(tempFile, text);

            String result = service.loadPreview(tempFile.toString());

            assertThat(result).isEqualTo(expected);

            Files.deleteIfExists(tempFile);
        }

        @ParameterizedTest
        @ValueSource(strings = {"nonexistent-preview.txt", "invalid-path-preview/file.mp4"})
        void testLoadPreviewWhenInvalidPathThenThrowsResourceNotFoundException(String invalidPath) {
            assertThatThrownBy(() -> service.loadPreview(invalidPath))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Video file not found at the specified path: " + invalidPath);
        }
    }

    @Nested
    class UploadTests {
        @Test
        void testUploadWhenValidFileThenSuccessfulUpload() throws IOException {
            MultipartFile mockMultipartFile = new MockMultipartFile(
                    "video",
                    "test-video.mp4",
                    "video/mp4",
                    new byte[]{1, 2, 3, 4}
            );

            String result = service.upload(mockMultipartFile);

            Path expectedPath = Path.of("uploads", mockMultipartFile.getOriginalFilename());
            assertAll(
                    () -> assertThat(result).isEqualTo(expectedPath.toString()),
                    () -> assertThat(Files.exists(expectedPath)).isTrue()
            );

            Files.deleteIfExists(expectedPath);
        }

        @Test
        void testUploadWhenInvalidFileNameThenThrowsException() {
            MultipartFile mockMultipartFile = new MockMultipartFile(
                    "video",
                    null,
                    "video/mp4",
                    new byte[]{1, 2, 3, 4}
            );

            assertThatThrownBy(() -> service.upload(mockMultipartFile)).isInstanceOf(InvalidFileUploadException.class);
        }

        @Test
        void testUploadWhenMalformedFileThenThrowsInvalidFileUploadException() throws IOException {
            MultipartFile mockMultipartFile = mock(MultipartFile.class);
            when(mockMultipartFile.getOriginalFilename()).thenReturn("malformed-video.mp4");
            when(mockMultipartFile.getBytes()).thenThrow(new MalformedInputException(123));

            assertAll(
                    () -> assertThatThrownBy(() -> service.upload(mockMultipartFile)).isInstanceOf(InvalidFileUploadException.class),
                    () -> verify(mockMultipartFile).getBytes()
            );
        }
    }

    @Nested
    class LoadTests {
        @Test
        void testLoadWhenValidPathThenReturnsFileContent() throws IOException {
            Path tempFile = Files.createTempFile("test-video", ".txt");
            String sampleContent = "sample content";
            Files.writeString(tempFile, sampleContent);
            String result = service.load(tempFile.toString());

            assertThat(result).isEqualTo(sampleContent);

            Files.deleteIfExists(tempFile);
        }

        @ParameterizedTest
        @ValueSource(strings = {"nonexistent-file.txt", "invalid-path/file.mp4"})
        void testLoadWhenInvalidPathThenThrowsResourceNotFoundException(String invalidPath) {
            assertThatThrownBy(() -> service.load(invalidPath))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Video file not found at the specified path: " + invalidPath);
        }
    }

    @Nested
    class DeleteTests {
        @Test
        void testDeleteWhenValidFileThenFileDeletedSuccessfully() throws IOException {
            Path tempFile = Files.createTempFile("test-video", ".mp4");

            service.delete(tempFile.toString());

            assertThat(Files.exists(tempFile)).isFalse();
        }
    }
}
