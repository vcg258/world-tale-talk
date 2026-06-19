package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;

public interface UserService {

    /**
     * 유저 단건 조회
     *
     * @param id 유저 PK
     * @return 유저 정보
     */
    UserDTO getUser(Long id);

    /**
     * 이메일로 유저 조회 (OAuth2 로그인시 기존 회원 여부 확인용)
     *
     * @param email 이메일
     * @return 유저 정보 (미가입시 null)
     */
    UserDTO getUserByEmail(String email);

    /**
     * 유저 생성 (OAuth2 최초 로그인시 자동 가입)
     *
     * @param userDTO 유저 정보
     */
    void createUser(UserDTO userDTO);

    /**
     * 회원 정보 수정
     *
     * @param userDTO 변경할 회원 정보
     */
    void modifyUser(UserDTO userDTO);

    /**
     * 계정 상태 변경 (정지 / 삭제 / 활성화) (관리자용)
     *
     * @param id     유저 PK
     * @param status 변경할 계정 상태
     */
    void modifyUserStatus(Long id, UserStatus status);

    /**
     * 닉네임 변경
     *
     * @param id       유저 PK
     * @param nickname 변경할 닉네임
     */
    void modifyNickname(Long id, String nickname);

    /**
     * 이메일 중복 여부 확인
     *
     * @param email 이메일
     * @return 중복 여부 (true: 중복)
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 중복 여부 확인
     *
     * @param nickname 닉네임
     * @return 중복 여부 (true: 중복)
     */
    boolean existsByNickname(String nickname);

    /**
     * 회원 탈퇴
     *
     * @param id 유저 PK
     */
    void deleteUser(Long id);
}
