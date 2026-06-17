package com.worldtaletalk.user.mapper;

import com.worldtaletalk.user.domain.UserSuspensionsEntity;
import com.worldtaletalk.user.dto.UserSuspensionsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserSuspensionsMapper {
    @Mapping(source = "users.id", target = "userId")
    @Mapping(source = "admin.id", target = "adminId")
    UserSuspensionsDTO toDTO(UserSuspensionsEntity userSuspensionsEntity);
    List<UserSuspensionsDTO> toDTOList(List<UserSuspensionsEntity> userSuspensionsEntity);
}
