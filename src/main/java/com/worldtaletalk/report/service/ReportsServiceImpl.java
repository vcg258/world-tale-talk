package com.worldtaletalk.report.service;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.chat.repository.ChatMessagesRepository;
import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import com.worldtaletalk.rating.repository.WorldRatingsRepository;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.report.ReportsMapper;
import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.report.domain.ReportsEntity;
import com.worldtaletalk.report.dto.ReportHistoryDTO;
import com.worldtaletalk.report.dto.ReportsDTO;
import com.worldtaletalk.report.repository.ReportsRepository;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.domain.user.UserRole;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportsServiceImpl implements ReportsService {
    private final ReportsRepository reportsRepository;
    private final ReportsMapper reportsMapper;
    private final UserRepository userRepository;
    private final WorldRatingsRepository worldRatingsRepository;
    private final WorldRatingsService worldRatingsService;
    private final ChatMessagesRepository chatMessagesRepository;


    /**
     * 처리 상태별 신고 목록 조회 (관리자용) (페이징)
     *
     * @param status 처리 상태
     * @return 상태별 신고 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<ReportsDTO> getReports(ReportStatus status, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReportsEntity> reports = reportsRepository.findAllByStatus(status, pageable);
        return reports.map(report -> enrichTargetInformation(reportsMapper.toDTO(report)));
    }

    /**
     * 신고 등록
     *
     * @param reportsDTO 신고 정보
     */
    @Transactional
    @Override
    public void addReport(ReportsDTO reportsDTO) {
        UserEntity user = userRepository.findById(reportsDTO.getUserId()).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재 하지 않습니다."));

        // 중복 신고 예외
        if (reportsRepository.existsByUserIdAndReport(
                reportsDTO.getUserId(), reportsDTO.getTargetType(), reportsDTO.getTargetId())) {
            throw new IllegalStateException("이미 신고한 대상입니다.");
        }

        ReportsEntity report = ReportsEntity.createReport(
                reportsDTO.getTargetType(),
                reportsDTO.getTargetId(),
                reportsDTO.getReason(),
                user
        );

        ReportsEntity save = reportsRepository.save(report);
        log.info("신고 등록 완료 : {}", save);
    }

    /**
     * 신고 처리
     *
     * @param reportsDTO 신고 처리 정보
     */
    @Transactional
    @Override
    public void handleReport(ReportsDTO reportsDTO) {
        if (reportsDTO.getStatus() == ReportStatus.PENDING) {
            throw new IllegalArgumentException("처리 상태는 RESOLVED 또는 DISMISSED만 가능합니다.");
        }
        ReportsEntity report = reportsRepository.findById(reportsDTO.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 신고 내역이 존재하지 않습니다."));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        UserEntity admin = userRepository.findById(reportsDTO.getAdminId()).orElseThrow(() ->
                new NoSuchElementException("해당 관리자는 존재 하지 않습니다."));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("해당 계정은 관리자 계정이 아닙니다.");
        }

        report.modifyInformationReport(reportsDTO.getStatus(), admin);

        ReportsEntity save = reportsRepository.save(report);
        log.info("신고 처리 완료 : {}", save);
    }


    /**
     * 신고된 리뷰 삭제 후 신고 처리 완료
     *
     * @param reportId 신고 PK
     * @param adminId 관리자 PK
     */
    @Transactional
    @Override
    public void deleteReportedReview(Long reportId, Long adminId) {
        ReportsEntity report = reportsRepository.findById(reportId).orElseThrow(() ->
                new NoSuchElementException("해당 신고 내역이 존재하지 않습니다."));

        if (report.getTargetType() != ReportTargetType.REVIEW) {
            throw new IllegalStateException("리뷰 신고만 삭제 처리할 수 있습니다.");
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        UserEntity admin = userRepository.findById(adminId).orElseThrow(() ->
                new NoSuchElementException("해당 관리자는 존재 하지 않습니다."));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("해당 계정은 관리자 계정이 아닙니다.");
        }

        worldRatingsService.deleteWorldRating(report.getTargetId());
        report.modifyInformationReport(ReportStatus.RESOLVED, admin);
        log.info("신고된 리뷰 삭제 및 신고 처리 완료 reportId : {}, ratingId : {}", reportId, report.getTargetId());
    }

    /**
     * 신고 내역 삭제
     *
     * @param id PK
     */
    @Transactional
    @Override
    public void deleteReport(Long id) {
        ReportsEntity report = reportsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 신고 내역이 존재하지 않습니다."));
        reportsRepository.delete(report);

        log.info("신고 내역 삭제 완료 : {}", report);
    }
    private ReportsDTO enrichTargetInformation(ReportsDTO report) {
        fillReportHistory(report);
        if (report.getTargetType() == ReportTargetType.REVIEW) {
            worldRatingsRepository.findById(report.getTargetId()).ifPresentOrElse(rating -> {
                String review = rating.getReview();
                String text = review == null || review.isBlank() ? "작성된 리뷰 없이 " + rating.getScore() + "점만 등록된 리뷰" : review;
                report.setTargetText("[" + rating.getScore() + "점] " + text);
                report.setTargetLink("/admin/ratings?worldId=" + rating.getWorld().getId());
                report.setTargetSessionId(rating.getStorySession().getId());
            }, () -> {
                report.setTargetText("이미 삭제되었거나 찾을 수 없는 리뷰입니다.");
                report.setTargetLink(null);
            });
            return report;
        }

        if (report.getTargetType() == ReportTargetType.CHAT_MESSAGE) {
            chatMessagesRepository.findByIdWithStorySessionDetails(report.getTargetId()).ifPresentOrElse(message -> {
                report.setTargetText(buildChatTargetText(message));
                report.setTargetLink("/admin/report/chat/" + message.getId());
                report.setTargetSessionId(message.getStorySession().getId());
            }, () -> {
                report.setTargetText("이미 삭제되었거나 찾을 수 없는 채팅 메시지입니다.");
                report.setTargetLink(null);
            });
        }
        return report;
    }


    private void fillReportHistory(ReportsDTO report) {
        report.setCumulativeReportCount(reportsRepository.countByTargetTypeAndTargetId(
                report.getTargetType(),
                report.getTargetId()
        ));
        report.setHistories(reportsRepository.findAllByTarget(report.getTargetType(), report.getTargetId()).stream()
                .map(history -> new ReportHistoryDTO(
                        history.getId(),
                        maskNickname(history.getUsers().getNickname()),
                        history.getReason(),
                        history.getCreatedAt()
                ))
                .toList());
    }

    private String maskNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "알 수 없음";
        }
        if (nickname.length() == 1) {
            return "*";
        }
        if (nickname.length() == 2) {
            return nickname.charAt(0) + "*";
        }
        return nickname.charAt(0) + "*".repeat(nickname.length() - 2) + nickname.charAt(nickname.length() - 1);
    }

    private String buildChatTargetText(ChatMessagesEntity message) {
        String content = message.getContent();
        if (content == null || content.isBlank()) {
            content = "내용 없음";
        }
        String shortContent = content.length() > 80 ? content.substring(0, 80) + "..." : content;
        return "세션 " + message.getStorySession().getId() + " / " + message.getRole() + " 턴 " + message.getTurn() + " - " + shortContent;
    }

}
