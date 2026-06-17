package com.worldtaletalk.user.mapper;

import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.dto.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(UserEntity userEntity);
    List<UserDTO> toDTOList(List<UserEntity> userEntity);
}