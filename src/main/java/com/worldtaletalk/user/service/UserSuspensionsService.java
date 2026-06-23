package com.worldtaletalk.user.service;

import com.worldtaletalk.user.dto.UserSuspensionsDTO;
import org.springframework.data.domain.Page;

import java.util.List;


public interface UserSuspensionsService {

    /**
     * 특정 유저의 정지 이력 조회 (관리자용) (페이징)
     *
     * @param userId 유저 PK
     * @param page   페이지 번호
     * @return 정지 이력 목록
     */
    Page<UserSuspensionsDTO> getUserSuspensions(Long userId, int page);

    /**
     * 특정 유저의 정지 이력 전체 조회 (관리자 상세 페이지용)
     *
     * @param userId 유저 PK
     * @return 정지 이력 전체 목록
     */
    List<UserSuspensionsDTO> getAllUserSuspensions(Long userId);

    /**
     * 해당 회원의 유효한 정지 이력 단건 조회
     *
     * @param userId 회원 PK
     * @return 회원 정지 이력
     */
    UserSuspensionsDTO getUserSuspension(Long userId);

    /**
     * 유저 정지 처리 (관리자용)
     *
     * @param userSuspensionsDTO 정지 정보
     */
    void addUserSuspension(UserSuspensionsDTO userSuspensionsDTO);

    /**
     * 회원 정지 해제 (관리자용)
     *
     * @param suspensionId 정지 이력 PK
     */
    void liftUserSuspension(Long suspensionId);
}
