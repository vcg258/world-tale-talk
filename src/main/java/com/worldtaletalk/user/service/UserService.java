package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;

public interface UserService {

    /**
     * 유저 단건 조회
     * @param id 유저 PK
     * @return 유저 정보
     */
    UserDTO getUser(Long id);

    /**
     * 이메일로 유저 조회 (OAuth2 로그인시 기존 회원 여부 확인용)
     * @param email 이메일
     * @return 유저 정보 (미가입시 null)
     */
    UserDTO getUserByEmail(String email);

    /**
     * 유저 생성 (OAuth2 최초 로그인시 자동 가입)
     * @param userDTO 유저 정보
     */
    void createUser(UserDTO userDTO);

    /**
     * 회원 정보 수정
     * @param userDTO 변경할 회원 정보
     */
    void modifyUser(UserDTO userDTO);

    /**
     * 계정 상태 변경 (정지 / 삭제 / 활성화) (관리자용)
     * @param id     유저 PK
     * @param status 변경할 계정 상태
     */
    void modifyUserStatus(Long id, UserStatus status);

    /**
     * 회원 탈퇴
     * @param id 유저 PK
     */
    void deleteUser(Long id);
}
