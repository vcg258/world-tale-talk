package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import org.springframework.data.domain.Page;

public interface UserService {

    /**
     * 전체 회원 조회 (탈퇴 회원 제외)
     * @param q 검색어
     * @param status 회원 상태
     * @param page 페이지
     * @return 회원 목록
     */
    Page<UserDTO> getUsers(String q, UserStatus status, int page);

    /**
     * 전체 회원 조회 (탈퇴 회원만)
     * @param q 검색어
     * @param page 페이지
     * @return 회원 목록
     */
    Page<UserDTO> getDelUsers(String q, int page);

    /**
     * 유저 단건 조회
     *
     * @param id 유저 PK
     * @return 유저 정보
     */
    UserDTO getUser(Long id);

    /**
     * 이메일로 유저 조회 (OAuth2 기존 회원 계정 중복 확인용)
     *
     * @param email 이메일
     * @return 유저 정보 (미가입시 null)
     */
    UserDTO getUserByEmail(String email);

    /**
     * 로그인 아이디로 유저 조회
     *
     * @param loginId 로그인 아이디
     * @return 유저 정보 (미가입시 null)
     */
    UserDTO getUserByLoginId(String loginId);

    /**
     * 로그인 아이디 + 이메일 동시 조회 (비밀번호 찾기 본인 확인용)
     *
     * @param loginId 로그인 아이디
     * @param email   이메일
     * @return 유저 정보 (불일치시 null)
     */
    UserDTO getUserByLoginIdAndEmail(String loginId, String email);

    /**
     * 유저 생성 (OAuth2전용 회원가입)
     *
     * @param userDTO 유저 정보
     */
    UserDTO createUser(UserDTO userDTO);

    /**
     * 회원가입 (일반 회원가입)
     *
     * @param userDTO 가입할 유저 정보
     * @param confirmPassword 비밀번호 확인
     * @return 생성된 유저 정보
     */
    UserDTO signUp(UserDTO userDTO, String confirmPassword);

    /**
     * 비밀번호 변경 (일반 회원 전용, 소셜 전용 계정은 대상 아님)
     *
     * @param id              유저 PK
     * @param newPassword     변경할 새 비밀번호
     * @param currentPassword 현재 비밀번호 (재확인용)
     * @param confirmPassword 새 비밀번호 재입력
     */
    void modifyPassword(Long id, String newPassword, String currentPassword, String confirmPassword);

    /**
     * 비밀번호 재설정 (비밀번호 찾기 기능)
     *
     * @param id              유저 PK
     * @param newPassword     새 비밀번호
     * @param confirmPassword 새 비밀번호 확인
     */
    void resetPassword(Long id, String newPassword, String confirmPassword);

    /**
     * 이메일 변경 (OTP 인증 완료 후 호출)
     *
     * @param id              유저 PK
     * @param newEmail        변경할 새 이메일 (OTP 인증 완료된 이메일)
     * @param currentPassword 현재 비밀번호 (재확인용, 소셜 전용 계정은 검증 생략)
     */
    void modifyEmail(Long id, String newEmail, String currentPassword);

    /**
     * 현재 비밀번호 재확인
     *
     * @param id              유저 PK
     * @param currentPassword 현재 비밀번호
     */
    void verifyCurrentPassword(Long id, String currentPassword);

    /**
     * 휴면 계정 해제 (본인 재로그인을 통한 자기 자신 활성화 전용)
     *
     * @param id 유저 PK
     */
    void reactivateUser(Long id);

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
     * 로그인 아이디 중복 여부 확인
     *
     * @param loginId 로그인 아이디
     * @return 중복 여부 (true: 중복)
     */
    boolean existsByLoginId(String loginId);

    /**
     * 회원 탈퇴
     *
     * @param id 유저 PK
     */
    void deleteUser(Long id);
}
