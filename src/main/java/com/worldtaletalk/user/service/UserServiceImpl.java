package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.socialaccounts.SocialAccountsEntity;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.mapper.UserMapper;
import com.worldtaletalk.user.repository.SocialAccountsRepository;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccountsRepository socialAccountsRepository;

    /**
     * 전체 회원 조회
     *
     * @param q      검색어
     * @param status 회원 상태
     * @param page   페이지
     * @return 회원 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<UserDTO> getUsers(String q, UserStatus status, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.ASC, "updatedAt"));
        Page<UserEntity> users = userRepository.findAllBySearch(q, status, pageable);
        return users.map(userMapper::toDTO);
    }

    /**
     * 유저 단건 조회
     *
     * @param id 유저 PK
     * @return 유저 정보
     */
    @Transactional(readOnly = true)
    @Override
    public UserDTO getUser(Long id) {
        return userRepository.findById(id).map(userMapper::toDTO).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));
    }

    /**
     * 이메일로 유저 조회 (OAuth2 기존 회원 계정 중복 확인용)
     *
     * @param email 이메일
     * @return 유저 정보 (미가입시 null)
     */
    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDTO).orElse(null);
    }

    /**
     * 로그인 아이디로 유저 조회
     *
     * @param loginId 로그인 아이디
     * @return 유저 정보 (미가입시 null)
     */
    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId).map(userMapper::toDTO).orElse(null);
    }

    /**
     * 로그인 아이디 + 이메일 동시 조회 (비밀번호 찾기 본인 확인용)
     *
     * @param loginId 로그인 아이디
     * @param email   이메일
     * @return 유저 정보 (불일치시 null)
     */
    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserByLoginIdAndEmail(String loginId, String email) {
        return userRepository.findByLoginIdAndEmail(loginId, email).map(userMapper::toDTO).orElse(null);
    }

    /**
     * 유저 생성 (OAuth2전용 회원가입)
     *
     * @param userDTO 유저 정보
     */
    @Transactional
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO.getNickname() == null || userDTO.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 값입니다.");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 값입니다.");
        }

        UserEntity user = UserEntity.createUser(
                userDTO.getNickname(),
                userDTO.getEmail()
        );
        UserEntity save = userRepository.save(user);
        log.info("회원가입 완료 : {}", save);

        return userMapper.toDTO(save);
    }

    /**
     * 회원가입 (일반 회원가입)
     *
     * @param userDTO         가입할 유저 정보
     * @param confirmPassword 비밀번호 확인
     * @return 생성된 유저 정보
     */
    @Transactional
    @Override
    public UserDTO signUp(UserDTO userDTO, String confirmPassword) {
        if (userDTO.getLoginId() == null || userDTO.getLoginId().trim().isEmpty()) {
            throw new IllegalArgumentException("아이디는 필수 값입니다.");
        }
        if (userDTO.getNickname() == null || userDTO.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 값입니다.");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 값입니다.");
        }
        if (userRepository.existsByLoginId(userDTO.getLoginId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(userDTO.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        if (userDTO.getPassword() == null || !userDTO.getPassword().equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (userDTO.getPassword().length() < 8 || userDTO.getPassword().length() > 50) {
            throw new IllegalArgumentException("비밀번호는 8~50자로 입력해주세요.");
        }
        if (containsHangul(userDTO.getPassword())) {
            throw new IllegalArgumentException("비밀번호에 한글은 사용할 수 없습니다.");
        }

        UserEntity user = UserEntity.createBasicUser(
                userDTO.getLoginId(),
                userDTO.getNickname(),
                userDTO.getEmail(),
                passwordEncoder.encode(userDTO.getPassword())
        );
        UserEntity save = userRepository.save(user);
        log.info("일반 회원가입 완료 : {}", save);

        return userMapper.toDTO(save);
    }

    /**
     * 비밀번호 변경 (일반 회원 전용, 소셜 전용 계정은 대상 아님)
     *
     * @param id              유저 PK
     * @param newPassword     변경할 새 비밀번호
     * @param currentPassword 현재 비밀번호 (재확인용)
     * @param confirmPassword 새 비밀번호 재입력
     */
    @Transactional
    @Override
    public void modifyPassword(Long id, String newPassword, String currentPassword, String confirmPassword) {
        UserEntity user = userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));
        validateCurrentPassword(user, currentPassword);

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }
        if (newPassword.length() < 8 || newPassword.length() > 50) {
            throw new IllegalArgumentException("비밀번호는 8~50자로 입력해주세요.");
        }
        if (containsHangul(newPassword)) {
            throw new IllegalArgumentException("비밀번호에 한글은 사용할 수 없습니다.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 다른 비밀번호를 입력해주세요.");
        }

        user.modifyInformationUser(user.getEmail(), passwordEncoder.encode(newPassword));
        UserEntity save = userRepository.save(user);
        log.info("비밀번호 변경 완료 : {}", save);
    }

    /**
     * 비밀번호 재설정 (비밀번호 찾기 기능)
     *
     * @param id              유저 PK
     * @param newPassword     새 비밀번호
     * @param confirmPassword 새 비밀번호 확인
     */
    @Transactional
    @Override
    public void resetPassword(Long id, String newPassword, String confirmPassword) {
        UserEntity user = userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }
        if (newPassword.length() < 8 || newPassword.length() > 50) {
            throw new IllegalArgumentException("비밀번호는 8~50자로 입력해주세요.");
        }
        if (containsHangul(newPassword)) {
            throw new IllegalArgumentException("비밀번호에 한글은 사용할 수 없습니다.");
        }

        user.modifyInformationUser(user.getEmail(), passwordEncoder.encode(newPassword));
        UserEntity save = userRepository.save(user);
        log.info("비밀번호 재설정 완료 : {}", save);
    }

    /**
     * 이메일 변경 (OTP 인증 완료 후 호출)
     *
     * @param id              유저 PK
     * @param newEmail        변경할 새 이메일 (OTP 인증 완료된 이메일)
     * @param currentPassword 현재 비밀번호 (재확인용, 소셜 전용 계정은 검증 생략)
     */
    @Transactional
    @Override
    public void modifyEmail(Long id, String newEmail, String currentPassword) {
        UserEntity user = userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));

        // 소셜 전용 계정(비밀번호 없음)은 재확인 자체를 생략, 일반/연동 계정은 검증
        if (user.getPassword() != null) {
            validateCurrentPassword(user, currentPassword);
        }

        user.modifyInformationUser(newEmail, user.getPassword());
        UserEntity save = userRepository.save(user);
        log.info("이메일 변경 완료 : {}", save);
    }

    /**
     * 현재 비밀번호 재확인
     *
     * @param id              유저 PK
     * @param currentPassword 현재 비밀번호
     */
    @Transactional(readOnly = true)
    @Override
    public void verifyCurrentPassword(Long id, String currentPassword) {
        UserEntity user = userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));
        validateCurrentPassword(user, currentPassword);
    }

    /**
     * 계정 상태 변경 (정지 / 삭제 / 활성화)
     *
     * @param id     유저 PK
     * @param status 변경할 계정 상태
     */
    @Transactional
    @Override
    public void modifyUserStatus(Long id, UserStatus status) {
        UserEntity user = userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));
        user.changeStatus(status);
        UserEntity save = userRepository.save(user);
        log.info("회원 계정 상태 변경 완료 : {}", save);
    }

    /**
     * 닉네임 변경
     *
     * @param id       유저 PK
     * @param nickname 변경할 닉네임
     */
    @Transactional
    @Override
    public void modifyNickname(Long id, String nickname) {
        UserEntity user = userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));

        // 중복 예외처리
        if (!nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        user.changeNickname(nickname);
        UserEntity save = userRepository.save(user);
        log.info("닉네임 변경 완료 : {}", save);
    }

    /**
     * 이메일 중복 여부 확인
     *
     * @param email 이메일
     * @return 중복 여부 (true: 중복)
     */
    @Transactional(readOnly = true)
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 여부 확인
     *
     * @param nickname 닉네임
     * @return 중복 여부 (true: 중복)
     */
    @Transactional(readOnly = true)
    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 로그인 아이디 중복 여부 확인
     *
     * @param loginId 로그인 아이디
     * @return 중복 여부 (true: 중복)
     */
    @Transactional(readOnly = true)
    @Override
    public boolean existsByLoginId(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    /**
     * 회원 탈퇴
     *
     * @param id 유저 PK
     */
    @Transactional
    @Override
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));
        user.userWithdrawal();
        UserEntity save = userRepository.save(user);

        if (!user.getSocialAccounts().isEmpty()) {
            List<SocialAccountsEntity> socialAccounts = user.getSocialAccounts();
            for (SocialAccountsEntity socialAccountsEntity : socialAccounts) {
                socialAccountsEntity.socialUserWithdrawal();
            }
            socialAccountsRepository.saveAll(socialAccounts);
        }

        log.info("회원 탈퇴로 인한 소셜 계정 연동 전체 익명화 처리 userId : {}", id);

        log.info("회원 탈퇴 완료 : {}", save);
    }


    /* Helper Methode */

    /**
     * 현재 비밀번호 재확인 (비밀번호 / 이메일 변경 공통 검증)
     *
     * @param user            유저 엔티티
     * @param currentPassword 입력받은 현재 비밀번호
     */
    private void validateCurrentPassword(UserEntity user, String currentPassword) {
        if (user.getPassword() == null) {
            throw new IllegalStateException("소셜 로그인 계정은 이 기능을 사용할 수 없습니다.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 비밀번호 한글 체크
     *
     * @param value 비밀번호
     * @return 한글 사용 여부
     */
    private boolean containsHangul(String value) {
        return value.chars().anyMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL);
    }
}
