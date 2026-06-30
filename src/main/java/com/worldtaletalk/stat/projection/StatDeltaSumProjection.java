package com.worldtaletalk.stat.projection;

// 스탯반환 합산을 위한 프로젝션 (엔티티로는 합산된 값을 넣을 수 도 없고 별명도 다르기 때문에 별도로 생성함)
public interface StatDeltaSumProjection {
    Integer getWisdomSum();
    Integer getStrengthSum();
    Integer getAgilitySum();
    Integer getCharismaSum();
    Integer getLuckSum();
}
