package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UsersDTO;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    /**
     * 유저 단건 조회
     * @param id 유저 PK
     * @return 유저 정보
     */
    @Transactional(readOnly = true)
    @Override
    public UsersDTO getUser(Long id) {
        return null;
    }

    /**
     * 이메일로 유저 조회 (OAuth2 로그인시 기존 회원 여부 확인용)
     * @param email 이메일
     * @return 유저 정보 (미가입시 null)
     */
    @Transactional(readOnly = true)
    @Override
    public UsersDTO getUserByEmail(String email) {
        return null;
    }

    /**
     * 유저 생성 (OAuth2 최초 로그인시 자동 가입)
     * @param usersDTO 유저 정보
     */
    @Transactional
    @Override
    public void createUser(UsersDTO usersDTO) {
    }

    /**
     * 닉네임 수정
     * @param id       유저 PK
     * @param nickname 변경할 닉네임
     */
    @Transactional
    @Override
    public void modifyUserNickname(Long id, String nickname) {
    }

    /**
     * 계정 상태 변경 (정지 / 삭제 / 활성화)
     * @param id     유저 PK
     * @param status 변경할 계정 상태
     */
    @Transactional
    @Override
    public void modifyUserStatus(Long id, UserStatus status) {
    }

    /**
     * 회원 탈퇴
     * @param id 유저 PK
     */
    @Transactional
    @Override
    public void deleteUser(Long id) {
    }
}
