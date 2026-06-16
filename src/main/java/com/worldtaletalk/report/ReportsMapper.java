package com.worldtaletalk.report;

import com.worldtaletalk.report.domain.ReportsEntity;
import com.worldtaletalk.report.dto.ReportsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportsMapper {
    @Mapping(source = "users.id", target = "userId")
    @Mapping(source = "admin.id", target = "adminId")
    ReportsDTO toDTO(ReportsEntity reportsEntity);
    List<ReportsDTO> toDTOList(List<ReportsEntity> reportsEntity);
}
