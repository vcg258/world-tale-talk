package com.worldtaletalk.statistics.service;

import com.worldtaletalk.statistics.dto.AdminDashboardStatsDTO;
import com.worldtaletalk.statistics.dto.WorldStatsDTO;

import java.util.List;

public interface AdminStatsService {

    /**
     * 어드민 대시보드 요약 통계 조회
     * @return 전체 회원 수, 세계관 수, 진행 중 세션 수, 처리 대기 신고 수
     */
    AdminDashboardStatsDTO getDashboardStats();

    /**
     * 세계관별 통계 목록 조회 (어드민용)
     * @return 세계관별 플레이 수, 완결/포기 수, 평균 별점
     */
    List<WorldStatsDTO> getWorldStats();
}
