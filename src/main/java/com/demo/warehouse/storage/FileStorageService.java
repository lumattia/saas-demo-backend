package com.demo.warehouse.storage;

import com.demo.warehouse.config.R2Properties;
import com.demo.warehouse.domain.FileInfo;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
public class FileStorageService {
    
    private final S3Client s3Client;
    private final R2Properties r2Properties;

    public FileStorageService(S3Client s3Client, R2Properties r2Properties) {
        this.s3Client = s3Client;
        this.r2Properties = r2Properties;
    }

     public FileInfo uploadFile(MultipartFile file, String folderPath) {
        String newKey = folderPath + "/" + UUID.randomUUID().toString() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(newKey)
                .contentType(file.getContentType())
                .build();
            s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileKey(newKey);
            fileInfo.setMimeType(file.getContentType());
            fileInfo.setSize(file.getSize());
            return fileInfo;
        } catch (IOException e) {
            throw new RuntimeException("Error al subir archivo", e);
        }
    }

    public void safeReplace(FileInfo oldFile, MultipartFile newFile, String folderPath, Consumer<FileInfo> dbUpdateCallback) {
        var newFileInfo = uploadFile(newFile, folderPath);
        try {
            dbUpdateCallback.accept(newFileInfo);
        } catch (Exception e) {
            deleteFileLog(newFileInfo);
            throw new RuntimeException("Error al actualizar la base de datos, cambio revertido", e);
        }
        if (oldFile != null) {
            deleteFileLog(oldFile);
        }
    }
    public void deleteFileLog(FileInfo fileInfo) {
        try {
            deleteFileThrow(fileInfo);
        } catch (RuntimeException e) {
            log.info("Error al borrar archivo: " + fileInfo.getFileKey());
        }
    }
    public void deleteFileThrow(FileInfo fileInfo) {
        if (fileInfo == null) return;
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(r2Properties.getBucketName())
                    .key(fileInfo.getFileKey())
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al borrar archivo", e);
        }
    }
}