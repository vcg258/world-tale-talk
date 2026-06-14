package com.worldtaletalk.item.service;

import com.worldtaletalk.item.dto.ItemGradeConfigsDTO;

import java.util.List;

public interface ItemGradeConfigsService {

    // 전체 등급 설정 조회
    List<ItemGradeConfigsDTO> getGradeConfigs();

    // 단건 조회
    ItemGradeConfigsDTO getGradeConfig(Long id);

    // 등급 설정 수정
    ItemGradeConfigsDTO modifyGradeConfig(Long id, ItemGradeConfigsDTO itemGradeConfigsDTO);
}
