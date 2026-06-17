package com.worldtaletalk.user.mapper;

import com.worldtaletalk.user.domain.socialaccounts.SocialAccountsEntity;
import com.worldtaletalk.user.dto.SocialAccountsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SocialAccountsMapper {
    @Mapping(source = "users.id", target = "userId")
    SocialAccountsDTO toDTO(SocialAccountsEntity socialAccountsEntity);
    List<SocialAccountsDTO> toDTOList(List<SocialAccountsEntity> socialAccountsEntity);
}
