package com.worldtaletalk.global.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class WorldSettingUtils {
    // 제목(# 로 시작하는 줄)에 이 문구가 있으면 그 절 전체를 히든 이벤트 조건으로 봄
    private static final String HIDDEN_EVENT_KEYWORD = "히든 이벤트";

    /**
     * 설정 텍스트에서 히든 이벤트 절을 뺀 공개 설정 조회
     * (히든 이벤트 조건이 RAG 청크로 서사 프롬프트에 들어가면 유저가 물었을 때 그대로 알려 주기 때문에 분리함)
     *
     * @param settingText 세계관 설정 텍스트
     * @return 공개 설정 (히든 이벤트 절이 없으면 원문 그대로)
     */
    public static String getPublicSetting(String settingText) {
        if (getHiddenEvent(settingText).isBlank()) {
            return settingText;
        }
        return collectSection(settingText, false);
    }

    /**
     * 설정 텍스트에서 히든 이벤트 조건 본문 조회
     * 히든 절은 제목 줄부터 다음 제목 줄 직전까지이며, 제목이 없는 평문 설정에는 히든 이벤트가 없는 것으로 봄
     *
     * @param settingText 세계관 설정 텍스트
     * @return 히든 이벤트 조건 본문 (제목 줄 제외, 없으면 빈 문자열)
     */
    public static String getHiddenEvent(String settingText) {
        return collectSection(settingText, true);
    }

    /**
     * 히든 이벤트 절 또는 그 외 절의 줄만 모아서 반환
     *
     * @param settingText   세계관 설정 텍스트
     * @param hiddenSection true면 히든 이벤트 절, false면 히든 이벤트 절을 뺀 나머지
     * @return 모은 줄 (앞뒤 공백 제거)
     */
    private static String collectSection(String settingText, boolean hiddenSection) {
        StringBuilder section = new StringBuilder();
        boolean inHiddenSection = false;

        for (String line : settingText.lines().toList()) {
            if (line.startsWith("#")) {
                inHiddenSection = line.contains(HIDDEN_EVENT_KEYWORD);
                if (inHiddenSection) {
                    continue; // 제목 줄은 조건 설명이 아니므로 제외
                }
            }
            if (inHiddenSection == hiddenSection) {
                section.append(line).append("\n");
            }
        }
        return section.toString().strip();
    }
}
