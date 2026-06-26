package com.worldtaletalk.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayStateCacheDTO {
    private String currentStory; // 현재 스토리
    private List<String> choices; // 선택지
    private int progressPercent; // 진행도 %
}
