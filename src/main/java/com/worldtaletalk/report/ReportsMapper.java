package com.worldtaletalk.report;

import com.worldtaletalk.config.MapStructConfig;
import com.worldtaletalk.report.domain.ReportsEntity;
import com.worldtaletalk.report.dto.ReportsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface ReportsMapper {
    @Mapping(source = "users.id", target = "userId")
    @Mapping(source = "admin.id", target = "adminId")
    @Mapping(source = "admin.nickname", target = "adminNickname")
    @Mapping(target = "targetText", ignore = true)
    @Mapping(target = "targetLink", ignore = true)
    @Mapping(target = "targetSessionId", ignore = true)
    @Mapping(target = "cumulativeReportCount", ignore = true)
    @Mapping(target = "histories", ignore = true)
    ReportsDTO toDTO(ReportsEntity reportsEntity);
    List<ReportsDTO> toDTOList(List<ReportsEntity> reportsEntity);
}
