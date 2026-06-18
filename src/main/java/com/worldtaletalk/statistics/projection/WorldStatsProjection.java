package com.worldtaletalk.statistics.projection;

import java.math.BigDecimal;

// 통계테이블이 존재 하지 않으므로 레파짓토리에 DTO를 받을 수 는 없으니 프로젝션을 사용함
public interface WorldStatsProjection {
    Long getWorldId();
    String getWorldName();
    Long getTotalSessions();
    Long getCompletedSessions();
    Long getAbandonedSessions();
    BigDecimal getRatingAvg();
}
