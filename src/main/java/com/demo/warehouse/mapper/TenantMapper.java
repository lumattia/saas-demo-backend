package com.demo.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.mapper.TenantDtos.TenantResponse;

@Mapper(componentModel = "spring")
public interface TenantMapper {
    @Mapping(source = "logo.fileKey", target = "logoUrl")
    @Mapping(source = "logo.fileName", target = "logoFileName")
    TenantResponse toResponse(Tenant entity);
}
