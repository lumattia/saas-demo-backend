package com.demo.warehouse.service;

import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.demo.warehouse.config.R2Properties;
import com.demo.warehouse.domain.FileInfo;
import com.demo.warehouse.mapper.FileInfoDtos.FileInfoRequest;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class FileStorageService {
    
    private final S3Client s3Client;
    private final R2Properties r2Properties;

    public FileStorageService(S3Client s3Client, R2Properties r2Properties) {
        this.s3Client = s3Client;
        this.r2Properties = r2Properties;
    }
    public byte[] parseBase64(String base64){
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }
        byte[][] fileBytesContainer = { Base64.getDecoder().decode(base64) };
        return fileBytesContainer[0];
    }

    public FileInfo uploadFile(FileInfoRequest file, String folderPath) {
        String extension = "";
        int lastDotIndex = file.fileName().lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = file.fileName().substring(lastDotIndex);
        }
        String newKey = folderPath + "/" + UUID.randomUUID().toString() + extension;
        try {
            byte[] bytes = parseBase64(file.base64());
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(newKey)
                .contentType(file.contentType())
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileKey(newKey);
            fileInfo.setFileName(file.fileName());
            fileInfo.setMimeType(file.contentType());
            fileInfo.setSize((long) bytes.length);
            
            return fileInfo;

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("El String Base64 enviado tiene un formato inválido", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir el archivo decodificado a R2/S3", e);
        }
    }

    public void safeReplace(FileInfo oldFile, FileInfoRequest newFile, String folderPath, Consumer<FileInfo> dbUpdateCallback) {
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