package com.worldtaletalk.notice;

import com.worldtaletalk.notice.domain.NoticesEntity;
import com.worldtaletalk.notice.dto.NoticesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoticesMapper {
    @Mapping(source = "admin.id", target = "adminId")
    NoticesDTO toDTO(NoticesEntity noticesEntity);
    List<NoticesDTO> toDTOList(List<NoticesEntity> noticesEntity);
}
