package com.worldtaletalk.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayStateCacheDTO {
    private String currentStory; // 현재 스토리
    private String userInput; // 현재 스토리를 만든 유저 입력
    private List<String> choices; // 선택지
    private Map<String, Map<String, Integer>> statChangesByChoice; // 선택지별 예상 스탯 변화량
    private int progressPercent; // 진행도 %
}
