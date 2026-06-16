package com.worldtaletalk.notice.service;

import com.worldtaletalk.notice.dto.NoticesDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NoticesService {

    /**
     * 공개 공지 목록 조회 (is_active = true 만 조회) (페이징)
     * @param page 페이지
     * @return 활성 공지 목록
     */
    Page<NoticesDTO> getActiveNotices(int page);

    /**
     * 전체 공지 목록 조회 (관리자용 - 비공개 포함) (페이징)
     * @param page 페이지
     * @return 전체 공지 목록
     */
    Page<NoticesDTO> getAllNotices(int page);

    /**
     * 공지 단건 조회
     * @param id 공지 PK
     * @return 공지 정보
     */
    NoticesDTO getNotice(Long id);

    /**
     * 공지 작성 (관리자용)
     * @param noticesDTO 작성할 공지 정보 (관리자 PK, 제목, 본문 포함)
     */
    void createNotice(NoticesDTO noticesDTO);

    /**
     * 공지 수정 (관리자용)
     * @param noticesDTO 수정할 공지 정보
     */
    void modifyNotice(NoticesDTO noticesDTO);


    /**
     * 공지 삭제 (관리자용)
     * @param id 공지 PK
     */
    void deleteNotice(Long id);
}
