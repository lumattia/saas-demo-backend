package com.demo.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.demo.warehouse.config.R2Properties;
import com.demo.warehouse.domain.FileInfo;
import com.demo.warehouse.mapper.FileInfoDtos.FileInfoResponse;

@Mapper(componentModel = "spring")
public abstract class FileInfoMapper {

    @Autowired
    protected R2Properties r2Properties;

    @Mapping(target = "url", expression = "java(resolvePublicUrl(entity))")
    @Mapping(source = "fileName", target = "originalFileName")
    public abstract FileInfoResponse toFileInfoResponse(FileInfo entity);

    protected String resolvePublicUrl(FileInfo file) {
        // Get the key here to avoid a bug that adds url before "name"
        var fileKey = file.getFileKey();
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        String baseUrl = r2Properties.getPublicUrl();
        
        if (baseUrl.endsWith("/")) {
            return baseUrl + fileKey;
        }
        return baseUrl + "/" + fileKey;
    }
}