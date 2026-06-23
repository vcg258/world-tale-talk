package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.mapper.UserMapper;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 전체 회원 조회
     *
     * @param q      검색어
     * @param status 회원 상태
     * @param page   페이지
     * @return 회원 목록
     */
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
     * 이메일로 유저 조회 (OAuth2 로그인시 기존 회원 여부 확인용)
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
     * 유저 생성 (OAuth2 최초 로그인시 자동 가입)
     *
     * @param userDTO 유저 정보
     */
    @Transactional
    @Override
    public void createUser(UserDTO userDTO) {
        if (userDTO.getNickname() == null || userDTO.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 값입니다.");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 값입니다.");
        }

        UserEntity user = UserEntity.createUser(
                userDTO.getLoginId(), // 소셜 로그인 유저는 아이디가 필요없으므로 null 예외안함
                userDTO.getNickname(),
                userDTO.getEmail(),
                userDTO.getPassword() != null ? bCryptPasswordEncoder.encode(userDTO.getPassword()) : null
        );
        UserEntity save = userRepository.save(user);
        log.info("회원가입 완료 : {}", save);
    }

    /**
     * 회원 정보 수정
     *
     * @param userDTO 변경할 회원 정보
     */
    @Transactional
    @Override
    public void modifyUser(UserDTO userDTO) {
        UserEntity user = userRepository.findById(userDTO.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));
        user.modifyInformationUser(
                userDTO.getEmail() != null ? userDTO.getEmail() : user.getEmail(),
                userDTO.getPassword() != null ?
                        bCryptPasswordEncoder.encode(userDTO.getPassword()) : user.getPassword()
        );
        UserEntity save = userRepository.save(user);
        log.info("회원정보 수정 완료 : {}", save);
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
        log.info("회원 탈퇴 완료 : {}", save);
    }
}
