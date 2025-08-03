package com.klb.account_service.mapper;

import org.mapstruct.Mapper;

import com.klb.account_service.dto.request.ProfileCreationRequest;
import com.klb.account_service.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
