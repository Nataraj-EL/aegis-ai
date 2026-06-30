package com.aegis.backend.mapper;

import com.aegis.backend.dto.AuthResponse;
import com.aegis.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    AuthResponse toAuthResponse(User user, String accessToken, String refreshToken);
}
