package com.demo.warehouse.service;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.demo.warehouse.config.R2Properties;
import com.demo.warehouse.domain.FileInfo;
import com.demo.warehouse.mapper.FileInfoDtos.FileInfoRequest;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private R2Properties r2Properties;

    @InjectMocks
    private FileStorageService fileStorageService;

    private FileInfo fileInfo;

    @BeforeEach
    void setUp() {
        when(r2Properties.getBucketName()).thenReturn("test-bucket");
        
        fileInfo = new FileInfo();
        fileInfo.setFileKey("test-key/test.txt");
        fileInfo.setFileName("test.txt");
        fileInfo.setMimeType("text/plain");
        fileInfo.setSize(8L);
        
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);
    }

    @Test
    void parseBase64_ShouldDecodeBase64String() {
        String base64Data = Base64.getEncoder().encodeToString("test data".getBytes());
        byte[] result = fileStorageService.parseBase64(base64Data);
        
        assertNotNull(result);
        assertEquals("test data", new String(result));
    }

    @Test
    void parseBase64_ShouldHandleDataUriPrefix() {
        String base64Data = "data:text/plain;base64," + Base64.getEncoder().encodeToString("test data".getBytes());
        byte[] result = fileStorageService.parseBase64(base64Data);
        
        assertNotNull(result);
        assertEquals("test data", new String(result));
    }

    @Test
    void parseBase64_ShouldThrowExceptionForInvalidBase64() {
        String invalidBase64 = "invalid-base64-string";
        
        assertThrows(IllegalArgumentException.class, () -> fileStorageService.parseBase64(invalidBase64));
    }

    @Test
    void uploadFile_ShouldThrowExceptionForInvalidBase64() {
        FileInfoRequest invalidRequest = new FileInfoRequest("test.txt", "text/plain", "invalid-base64");
        
        assertThrows(RuntimeException.class, () -> fileStorageService.uploadFile(invalidRequest, "test-folder"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void deleteFileLog_ShouldDeleteFileSuccessfully() {
        fileStorageService.deleteFileLog(fileInfo);
        
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFileLog_ShouldLogErrorWhenDeletionFails() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
            .thenThrow(new RuntimeException("S3 error"));
        
        assertDoesNotThrow(() -> fileStorageService.deleteFileLog(fileInfo));
    }

    @Test
    void deleteFileLog_ShouldHandleNullFileInfo() {
        assertDoesNotThrow(() -> fileStorageService.deleteFileLog(null));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFileThrow_ShouldDeleteFileSuccessfully() {
        fileStorageService.deleteFileThrow(fileInfo);
        
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFileThrow_ShouldThrowExceptionWhenDeletionFails() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
            .thenThrow(new RuntimeException("S3 error"));
        
        assertThrows(RuntimeException.class, () -> fileStorageService.deleteFileThrow(fileInfo));
    }

    @Test
    void deleteFileThrow_ShouldHandleNullFileInfo() {
        assertDoesNotThrow(() -> fileStorageService.deleteFileThrow(null));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}
