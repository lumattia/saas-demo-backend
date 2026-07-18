package com.demo.warehouse.mapper;

public class FileInfoDtos {
    
    public static record FileInfoRequest(
        String base64,
        String fileName,
        String contentType
    ) {}

    public static record FileInfoResponse(
        String url,
        String originalFileName,
        long size
    ) {}
}
