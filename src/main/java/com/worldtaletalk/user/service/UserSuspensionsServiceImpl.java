package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.UserSuspensionsEntity;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserSuspensionsDTO;
import com.worldtaletalk.user.mapper.UserSuspensionsMapper;
import com.worldtaletalk.user.repository.UserRepository;
import com.worldtaletalk.user.repository.UserSuspensionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSuspensionsServiceImpl implements UserSuspensionsService {
    private final UserSuspensionsRepository userSuspensionsRepository;
    private final UserSuspensionsMapper userSuspensionsMapper;
    private final UserRepository userRepository;

    /**
     * 특정 유저의 정지 이력 조회 (관리자용) (페이징)
     * @param userId 유저 PK
     * @return 정지 이력 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<UserSuspensionsDTO> getUserSuspensions(Long userId, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "id"));
        return userSuspensionsRepository.findAllByUsersId(userId, pageable).map(userSuspensionsMapper::toDTO);
    }

    /**
     * 유저 정지 처리 (관리자용)
     * @param userSuspensionsDTO 정지 정보
     */
    @Transactional
    @Override
    public void addUserSuspension(UserSuspensionsDTO userSuspensionsDTO) {
        UserEntity user = userRepository.findById(userSuspensionsDTO.getUserId()).orElseThrow(() ->
                new NoSuchElementException("해당 회원은 존재 하지 않습니다."));
        UserEntity admin = userRepository.findById(userSuspensionsDTO.getAdminId()).orElseThrow(() ->
                new NoSuchElementException("해당 관리자는 존재 하지 않습니다."));
        if (!admin.getRole().equals(UserRole.ADMIN)) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        if (user.getStatus().equals(UserStatus.DELETED) || user.getStatus().equals(UserStatus.SUSPENDED)) {
            throw new IllegalArgumentException("이미 정지 된 상태이거나 삭제된 회원입니다. 정지 불가능");
        }
        UserSuspensionsEntity userSuspension = UserSuspensionsEntity.createUserSuspensionsEntity(
                user,
                admin,
                userSuspensionsDTO.getReason()
        );

        user.changeStatus(UserStatus.SUSPENDED);
        UserSuspensionsEntity save = userSuspensionsRepository.save(userSuspension);
        log.info("유저 정지 처리 완료 : {}", save);
    }

    /**
     * 회원 정지 해제 (관리자용)
     * @param suspensionId 정지 이력 PK
     */
    @Transactional
    @Override
    public void liftUserSuspension(Long suspensionId) {
        UserSuspensionsEntity userSuspension = userSuspensionsRepository.findById(suspensionId).orElseThrow(() ->
                new NoSuchElementException("해당 정지 이력은 존재하지 않습니다."));
        if (userSuspension.getLiftedAt() != null) {
            throw new IllegalArgumentException("이미 정지 해제 처리 된 정지 이력입니다.");
        }
        userSuspension.modifyUnstoppableTime();
        userSuspension.getUsers().changeStatus(UserStatus.ACTIVE);
        UserSuspensionsEntity save = userSuspensionsRepository.save(userSuspension);
        log.info("회원 정지 해제 완료 : {}", save);
    }
}
