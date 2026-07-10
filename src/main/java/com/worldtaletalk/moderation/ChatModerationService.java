package com.worldtaletalk.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.moderation.*;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatModerationService {
    private final ModerationModel moderationModel;

    /**
     * 자유 입력 텍스트 모더레이션 체크
     *
     * @param sessionId 세션 PK (로그 식별용)
     * @param userId    유저 PK (로그 식별용)
     * @param content   검사할 자유 입력 텍스트
     * @return 하드블록 대상이면 true, 통과하거나 모더레이션 API 실패 시 false
     */
    public boolean isBlocked(Long sessionId, Long userId, String content) {
        try {
            // 모더레이션 API 호출
            ModerationResponse response = moderationModel.call(new ModerationPrompt(content));
            Moderation moderation = response.getResult().getOutput();

            for (ModerationResult result : moderation.getResults()) {
                if (!result.isFlagged()) continue; // 어떤 카테고리에도 안 걸렸으면 통과

                // 서사적 맥락으로 정당화 안 되는 카테고리는 하드블록
                Categories categories = result.getCategories();
                if (ModerationCategory.isHardBlock(categories)) {
                    log.warn("모더레이션 하드블록 sessionId : {}, userId : {}, categories : {}", sessionId, userId, categories);
                    return true;
                }
                // 그 외 카테고리는 로그만 남기고 진행
                log.warn("모더레이션 플래그(로그만) sessionId: {}, userId: {}, categories: {}", sessionId, userId, categories);
            }

            return false;
        } catch (Exception e) {
            log.error("모더레이션 체크 실패 sessionId : {}, userId : {}", sessionId, userId, e);
            return false;
        }
    }
}
