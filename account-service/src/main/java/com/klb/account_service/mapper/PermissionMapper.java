package com.klb.account_service.mapper;

import org.mapstruct.Mapper;

import com.klb.account_service.dto.request.PermissionRequest;
import com.klb.account_service.dto.response.PermissionResponse;
import com.klb.account_service.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
