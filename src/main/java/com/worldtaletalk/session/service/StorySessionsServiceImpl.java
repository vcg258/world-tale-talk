package com.worldtaletalk.session.service;

import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.persona.repository.PersonasRepository;
import com.worldtaletalk.session.StorySessionsMapper;
import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.repository.UserRepository;
import com.worldtaletalk.world.domain.WorldsEntity;
import com.worldtaletalk.world.repository.WorldsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorySessionsServiceImpl implements StorySessionsService {
    private final StorySessionsRepository storySessionsRepository;
    private final StorySessionsMapper storySessionsMapper;
    private final UserRepository userRepository;
    private final WorldsRepository worldsRepository;
    private final PersonasRepository personasRepository;

    /**
     * 세션 여러건 조회 (관리자용)
     *
     * @return 세션s
     */
    @Transactional(readOnly = true)
    @Override
    public List<StorySessionsDTO> getStorySessions() {
        return storySessionsMapper.toDTOList(storySessionsRepository.findAllWithDetails());
    }

    /**
     * 세션 여러건 조회 (필터 적용)
     *
     * @param userId 회원 PK
     * @param worldId 세계관 PK
     * @param status 해당 세션 상태
     * @param page 페이지
     * @return 세션 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<StorySessionsDTO> getSessions(Long userId, Long worldId, SessionStatus status, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StorySessionsEntity> entities = storySessionsRepository.findAllByIdWithWorldIdAndStatus(userId, worldId, status, pageable);
        return entities.map(storySessionsMapper::toDTO);
    }

    /**
     * 세션 단건 조회
     *
     * @param id PK
     * @return 세션
     */
    @Transactional(readOnly = true)
    @Override
    public StorySessionsDTO getStorySession(Long id) {
        return storySessionsRepository.findById(id).map(storySessionsMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("해당 세션은 존재 하지 않습니다."));
    }

    /**
     * 해당 회원의 세션 여러건 조회
     *
     * @param userId 회원 PK
     * @param page 페이지 수
     * @return 회원 세션 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<StorySessionsDTO> getMySessions(Long userId, SessionStatus status, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<StorySessionsEntity> userSessions = storySessionsRepository.findAllByUserId(userId, status, pageable);
        return userSessions.map(storySessionsMapper::toDTO);
    }

    /**
     * 세션 생성 (새게임)
     *
     * @param storySessionsDTO 세션 정보
     * @return 생성된 세션 PK
     */
    @Transactional
    @Override
    public Long createStorySession(StorySessionsDTO storySessionsDTO) {
        UserEntity user = userRepository.findById(storySessionsDTO.getUserId()).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 찾을 수 없습니다."));

        WorldsEntity world = worldsRepository.findById(storySessionsDTO.getWorldId()).orElseThrow(() ->
                new NoSuchElementException("해당 세계관은 찾을 수 없습니다."));
        if (!world.isActive()) {
            throw new IllegalStateException("비활성화된 세계관에서는 새로운 세션을 시작할 수 없습니다.");
        }

        PersonasEntity persona = personasRepository.findById(storySessionsDTO.getPersonaId()).orElseThrow(() ->
                new NoSuchElementException("해당 페르소나는 찾을 수 없습니다."));
        if (!persona.isActive()) {
            throw new IllegalStateException("비활성화된 페르소나로는 새로운 세션을 시작할 수 없습니다.");
        }

        StorySessionsEntity storySessions = StorySessionsEntity.createSession(
                user,
                world,
                persona
        );
        StorySessionsEntity sessionSave = storySessionsRepository.save(storySessions);
        log.info("세션 생성 : {}", sessionSave);

        // 세션 생성후 플레이 수 ++
        world.playCountUp();
        WorldsEntity worldSave = worldsRepository.save(world);
        log.info("해당 세계관 플레이수 ++후 전체 값 : {}", worldSave.getPlayCount());

        return sessionSave.getId();
    }

    /**
     * 세션상태 변경
     *
     * @param id     PK
     * @param status 새션 상태
     */
    @Transactional
    @Override
    public void modifyStorySessionByStatus(Long id, SessionStatus status) {
        StorySessionsEntity storySession = storySessionsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        storySession.changeInformationByStorySession(status);

        StorySessionsEntity save = storySessionsRepository.save(storySession);
        log.info("세션 진행 상태 변경 : {}", save);
    }

    /**
     * 회원이 세션을 삭제 했을경우 처리 (관리자는 다시 복구 할 수 도 있도록 반전처리함)
     *
     * @param id 세션 PK
     */
    @Transactional
    @Override
    public void modifyHiddenByUserWithAdmin(Long id) {
        StorySessionsEntity storySession = storySessionsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));
        storySession.changeHiddenByUser();
        StorySessionsEntity save = storySessionsRepository.save(storySession);
        log.info("세션 뷰 노출처리 변경 완료 : {}", save);
    }

    /**
     * 회원이 세션을 삭제 했을경우 처리
     *
     * @param id 세션 PK
     * @param userId 회원 PK
     */
    @Transactional
    @Override
    public void modifyHiddenByUser(Long id, Long userId) {
        StorySessionsEntity storySession = storySessionsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));
        if (!storySession.getUsers().getId().equals(userId)) {
            throw new IllegalStateException("본인의 세션만 삭제할 수 있습니다.");
        }
        if (storySession.isHiddenByUser()) {
            throw new IllegalStateException("이미 삭제된 채팅방입니다.");
        }
        storySession.changeHiddenByUser();
        StorySessionsEntity save = storySessionsRepository.save(storySession);
        log.info("세션 뷰 노출처리 변경 완료 : {}", save);
    }

    /**
     * 세션 엔딩 결정
     *
     * @param id         PK
     * @param endingType 엔딩
     */
    @Transactional
    @Override
    public void modifyStorySessionByEndingType(Long id, EndingType endingType) {
        StorySessionsEntity storySession = storySessionsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        storySession.changeInformationByEndingType(endingType);

        StorySessionsEntity save = storySessionsRepository.save(storySession);
        log.info("세션 엔딩 결정 : {}", save);
    }

    /**
     * 스토리 요약 갱신
     *
     * @param sessionId      세션 PK
     * @param runningSummary 스토리 요약
     */
    @Transactional
    @Override
    public void modifyRunningSummary(Long sessionId, String runningSummary) {
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        storySession.changeRunningSummary(runningSummary);

        StorySessionsEntity save = storySessionsRepository.save(storySession);
        log.info("해당 턴마다 스토리 요약본 갱신 : {}", save);
    }

    /**
     * 세션 소유자 검증 (WebSocket 인가용)
     *
     * @param sessionId 세션 PK
     * @param userId    회원 PK
     * @return 소유자 여부
     */
    @Transactional(readOnly = true)
    @Override
    public boolean isOwner(Long sessionId, Long userId) {
        return storySessionsRepository.existsByIdAndUsersId(sessionId, userId);
    }

    /**
     * 세션 소유자 검증 (HTTP 요청)
     *
     * @param sessionId 세션 PK
     * @param userId    회원 PK
     */
    @Override
    public void validateOwner(Long sessionId, Long userId) {
        boolean exists = storySessionsRepository.existsByIdAndUsersId(sessionId, userId);
        if (userId == null || !exists) {
            throw new AccessDeniedException("본인 세션만 접근할 수 있습니다.");
        }
    }

    /**
     * 세션 삭제
     *
     * @param id PK
     */
    @Transactional
    @Override
    public void deleteStorySession(Long id) {
        StorySessionsEntity session = storySessionsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 세션은 존재하지 않습니다."));
        storySessionsRepository.deleteById(session.getId());
        log.info("세션 삭제 완료 : id={}", id);
    }
}