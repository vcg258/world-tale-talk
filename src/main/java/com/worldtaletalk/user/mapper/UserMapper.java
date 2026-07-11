package com.worldtaletalk.user.mapper;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.dto.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {
    UserDTO toDTO(UserEntity userEntity);
    List<UserDTO> toDTOList(List<UserEntity> userEntity);
}