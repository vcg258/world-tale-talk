package com.worldtaletalk.item.service;

import com.worldtaletalk.item.domain.ItemGradeConfigsEntity;
import com.worldtaletalk.item.dto.ItemGradeConfigsDTO;
import com.worldtaletalk.item.mapper.ItemGradeConfigsMapper;
import com.worldtaletalk.item.repository.ItemGradeConfigsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemGradeConfigsServiceImpl implements ItemGradeConfigsService {
    private final ItemGradeConfigsRepository itemGradeConfigsRepository;
    private final ItemGradeConfigsMapper itemGradeConfigsMapper;

    /**
     * 전체 등급 보정 설정 조회
     *
     * @return 등급 설정 목록
     */
    @Transactional(readOnly = true)
    @Override
    public List<ItemGradeConfigsDTO> getGradeConfigs() {
        return itemGradeConfigsMapper.toDTOList(itemGradeConfigsRepository.findAll());
    }

    /**
     * 단건 등급 보정 설정 조회
     *
     * @param id 등급 설정 PK
     * @return 등급 설정
     */
    @Transactional(readOnly = true)
    @Override
    public ItemGradeConfigsDTO getGradeConfig(Long id) {
        return itemGradeConfigsRepository.findById(id).map(itemGradeConfigsMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("해당 등급 설정이 존재하지 않습니다."));
    }

    /**
     * 등급 보정 설정 수정 (관리자)
     *
     * @param id  등급 설정 PK
     * @param itemGradeConfigsDTO 수정할 설정 값
     * @return 수정된 설정
     */
    @Transactional
    @Override
    public ItemGradeConfigsDTO modifyGradeConfig(Long id, ItemGradeConfigsDTO itemGradeConfigsDTO) {
        ItemGradeConfigsEntity entity = itemGradeConfigsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 등급 설정이 존재하지 않습니다."));

        entity.updateConfig(
                itemGradeConfigsDTO.getStatSumMax() != null ? itemGradeConfigsDTO.getStatSumMax() : entity.getStatSumMax(),
                itemGradeConfigsDTO.getSingleMax() != null ? itemGradeConfigsDTO.getSingleMax() : entity.getSingleMax(),
                itemGradeConfigsDTO.getSingleMin() != null ? itemGradeConfigsDTO.getSingleMin() : entity.getSingleMin(),
                itemGradeConfigsDTO.getCanNegative() != null ? itemGradeConfigsDTO.getCanNegative() : entity.isCanNegative()
        );

        ItemGradeConfigsEntity saved = itemGradeConfigsRepository.save(entity);
        log.info("등급 보정 설정 수정 완료 grade: {}, statSumMax: {}, singleMax: {}, singleMin: {}, canNegative: {}",
                saved.getGrade(), saved.getStatSumMax(), saved.getSingleMax(), saved.getSingleMin(), saved.isCanNegative());
        return itemGradeConfigsMapper.toDTO(saved);
    }
}
