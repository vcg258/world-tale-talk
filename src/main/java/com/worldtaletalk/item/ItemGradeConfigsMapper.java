package com.worldtaletalk.item;

import com.worldtaletalk.item.domain.ItemGradeConfigsEntity;
import com.worldtaletalk.item.dto.ItemGradeConfigsDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemGradeConfigsMapper {

    ItemGradeConfigsDTO toDTO(ItemGradeConfigsEntity itemGradeConfigsEntity);
    List<ItemGradeConfigsDTO> toDTOList(List<ItemGradeConfigsEntity> itemGradeConfigsEntity);

}
