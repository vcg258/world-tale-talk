package com.worldtaletalk.notice.service;

import com.worldtaletalk.notice.NoticesMapper;
import com.worldtaletalk.notice.domain.NoticesEntity;
import com.worldtaletalk.notice.dto.NoticesDTO;
import com.worldtaletalk.notice.repository.NoticesRepository;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticesServiceImpl implements NoticesService {
    private final NoticesRepository noticesRepository;
    private final NoticesMapper noticesMapper;
    private final UserRepository userRepository;

    /**
     * 공개 공지 목록 조회 (is_active = true 만 조회) (페이징)
     *
     * @return 활성 공지 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<NoticesDTO> getActiveNotices(int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NoticesEntity> notices = noticesRepository.findAllByActiveTrue(pageable);
        return notices.map(noticesMapper::toDTO);
    }

    /**
     * 전체 공지 목록 조회 (관리자용 - 비공개 포함) (페이징)
     *
     * @return 전체 공지 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<NoticesDTO> getAllNotices(int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NoticesEntity> notices = noticesRepository.findAll(pageable);
        return notices.map(noticesMapper::toDTO);
    }

    /**
     * 공지 단건 조회
     *
     * @param id 공지 PK
     * @return 공지 정보
     */
    @Transactional(readOnly = true)
    @Override
    public NoticesDTO getNotice(Long id) {
        return noticesRepository.findById(id).map(noticesMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("해당 공지사항은 존재 하지 않습니다."));
    }

    /**
     * 공지 작성 (관리자용)
     *
     * @param noticesDTO 작성할 공지 정보 (관리자 PK, 제목, 본문 포함)
     */
    @Transactional
    @Override
    public void createNotice(NoticesDTO noticesDTO) {
        UserEntity admin = userRepository.findById(noticesDTO.getAdminId()).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));

        NoticesEntity notice = NoticesEntity.createNotices(
                admin,
                noticesDTO.getTitle(),
                noticesDTO.getContent(),
                noticesDTO.getActive()
        );
        NoticesEntity save = noticesRepository.save(notice);
        log.info("공지글 생성 {}", save);
    }

    /**
     * 공지 수정 (관리자용)
     *
     * @param noticesDTO 수정할 공지 정보
     */
    @Transactional
    @Override
    public void modifyNotice(NoticesDTO noticesDTO) {
        NoticesEntity notice = noticesRepository.findById(noticesDTO.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 공지사항은 존재 하지 않습니다."));

        if (!Objects.equals(noticesDTO.getAdminId(), notice.getAdmin().getId())) {
            throw new IllegalStateException("해당 사용자가 작성한 공지글이 아닙니다.");
        }

        notice.modifyInformation(
                noticesDTO.getTitle() != null ? noticesDTO.getTitle() : notice.getTitle(),
                noticesDTO.getContent() != null ? noticesDTO.getContent() : notice.getContent(),
                noticesDTO.getActive() != null ? noticesDTO.getActive() : notice.isActive()
        );

        NoticesEntity save = noticesRepository.save(notice);
        log.info("공지글 수정 : {}", save);
    }


    /**
     * 공지 삭제 (관리자용)
     *
     * @param id 공지 PK
     */
    @Transactional
    @Override
    public void deleteNotice(Long id) {
        NoticesEntity notice = noticesRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 공지사항은 존재 하지 않습니다."));
        noticesRepository.delete(notice);
        log.info("삭제완료 : {}", notice);
    }
}
