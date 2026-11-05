package com.worldtaletalk.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldSettingUtilsTest {
    @Test
    @DisplayName("마지막 절이 히든 이벤트면 공개 설정에서 빠지고 본문만 히든 이벤트로 조회됨")
    void hiddenSectionAtEnd() {
        String settingText = "# 설정집\n\n## 1. 개요\n개요 본문이다.\n\n## 13. 히든 이벤트 (직접 언급하지 말 것)\n조건 본문이다.";

        String publicSetting = WorldSettingUtils.getPublicSetting(settingText);
        String hiddenEvent = WorldSettingUtils.getHiddenEvent(settingText);

        assertAll(
                () -> assertTrue(publicSetting.contains("개요 본문이다.")),
                () -> assertFalse(publicSetting.contains("조건 본문이다.")),
                () -> assertFalse(publicSetting.contains("히든 이벤트")),
                () -> assertEquals("조건 본문이다.", hiddenEvent)
        );
    }

    @Test
    @DisplayName("히든 이벤트 절이 중간에 있어도 다음 제목부터는 공개 설정으로 남음")
    void hiddenSectionInMiddle() {
        String settingText = "## 1. 개요\n개요 본문이다.\n## 2. 히든 이벤트\n조건 본문이다.\n## 3. 지역\n지역 본문이다.";

        String publicSetting = WorldSettingUtils.getPublicSetting(settingText);
        String hiddenEvent = WorldSettingUtils.getHiddenEvent(settingText);

        assertAll(
                () -> assertTrue(publicSetting.contains("개요 본문이다.")),
                () -> assertTrue(publicSetting.contains("지역 본문이다.")),
                () -> assertFalse(publicSetting.contains("조건 본문이다.")),
                () -> assertEquals("조건 본문이다.", hiddenEvent)
        );
    }

    @Test
    @DisplayName("히든 이벤트 절이 없으면 원문을 그대로 공개 설정으로 두고 히든 이벤트는 비어 있음")
    void withoutHiddenSection() {
        String settingText = "마법 고갈 사태를 맞이한 하이엘프 왕국. 플레이어는 마법진의 비밀을 추적한다.";

        assertAll(
                () -> assertEquals(settingText, WorldSettingUtils.getPublicSetting(settingText)),
                () -> assertEquals("", WorldSettingUtils.getHiddenEvent(settingText))
        );
    }

    @Test
    @DisplayName("줄바꿈이 CRLF여도 같은 결과로 조회됨")
    void withCrLf() {
        String settingText = "## 1. 개요\r\n개요 본문이다.\r\n## 2. 히든 이벤트\r\n조건 본문이다.";

        assertAll(
                () -> assertEquals("## 1. 개요\n개요 본문이다.", WorldSettingUtils.getPublicSetting(settingText)),
                () -> assertEquals("조건 본문이다.", WorldSettingUtils.getHiddenEvent(settingText))
        );
    }
}
