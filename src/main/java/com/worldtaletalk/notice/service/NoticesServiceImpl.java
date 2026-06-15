package com.worldtaletalk.notice.service;

import com.worldtaletalk.notice.dto.NoticesDTO;
import com.worldtaletalk.notice.repository.NoticesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticesServiceImpl implements NoticesService {
    private final NoticesRepository noticesRepository;

    /**
     * 공개 공지 목록 조회 (is_active = true 만 조회) (페이징)
     * @return 활성 공지 목록
     */
    @Override
    public List<NoticesDTO> getActiveNotices() {
        return List.of();
    }

    /**
     * 전체 공지 목록 조회 (관리자용 - 비공개 포함) (페이징)
     * @return 전체 공지 목록
     */
    @Override
    public List<NoticesDTO> getAllNotices() {
        return List.of();
    }

    /**
     * 공지 단건 조회
     * @param id 공지 PK
     * @return 공지 정보
     */
    @Override
    public NoticesDTO getNotice(Long id) {
        return null;
    }

    /**
     * 공지 작성 (관리자용)
     * @param noticesDTO 작성할 공지 정보 (관리자 PK, 제목, 본문 포함)
     */
    @Override
    public void createNotice(NoticesDTO noticesDTO) {
    }

    /**
     * 공지 수정 (관리자용)
     * @param id         공지 PK
     * @param noticesDTO 수정할 공지 정보
     */
    @Override
    public void modifyNotice(Long id, NoticesDTO noticesDTO) {
    }

    /**
     * 공지 공개 / 비공개 전환 (관리자용)
     * @param id     공지 PK
     * @param active 공개 여부 (true = 공개)
     */
    @Override
    public void modifyNoticeActive(Long id, boolean active) {
    }

    /**
     * 공지 삭제 (관리자용)
     * @param id 공지 PK
     */
    @Override
    public void deleteNotice(Long id) {
    }
}
