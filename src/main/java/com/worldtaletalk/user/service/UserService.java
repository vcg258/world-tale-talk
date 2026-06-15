package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UsersDTO;

public interface UserService {

    /**
     * 유저 단건 조회
     * @param id 유저 PK
     * @return 유저 정보
     */
    UsersDTO getUser(Long id);

    /**
     * 이메일로 유저 조회 (OAuth2 로그인시 기존 회원 여부 확인용)
     * @param email 이메일
     * @return 유저 정보 (미가입시 null)
     */
    UsersDTO getUserByEmail(String email);

    /**
     * 유저 생성 (OAuth2 최초 로그인시 자동 가입)
     * @param usersDTO 유저 정보
     */
    void createUser(UsersDTO usersDTO);

    /**
     * 닉네임 수정
     * @param id       유저 PK
     * @param nickname 변경할 닉네임
     */
    void modifyUserNickname(Long id, String nickname);

    /**
     * 계정 상태 변경 (정지 / 삭제 / 활성화)
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
