package com.worldtaletalk.user.service;

import com.worldtaletalk.user.dto.UserSuspensionsDTO;

import java.util.List;

public interface UserSuspensionsService {

    /**
     * 특정 유저의 정지 이력 조회 (관리자용) (페이징)
     * @param userId 유저 PK
     * @return 정지 이력 목록
     */
    List<UserSuspensionsDTO> getUserSuspensions(Long userId);

    /**
     * 유저 정지 처리 (관리자용)
     * @param userSuspensionsDTO 정지 정보 (유저 PK, 관리자 PK, 사유 포함)
     */
    void addUserSuspension(UserSuspensionsDTO userSuspensionsDTO);

    /**
     * 유저 정지 해제 (관리자용 - liftedAt 업데이트)
     * @param suspensionId 정지 이력 PK
     */
    void liftUserSuspension(Long suspensionId);
}
