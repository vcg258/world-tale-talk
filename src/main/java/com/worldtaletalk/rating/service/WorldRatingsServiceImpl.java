package com.worldtaletalk.rating.service;

import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.repository.WorldRatingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldRatingsServiceImpl implements WorldRatingsService {
    private final WorldRatingsRepository worldRatingsRepository;

    /**
     * 세계관 별점 목록 조회 (페이징)
     * @param worldId 세계관 PK
     * @return 해당 세계관의 별점 목록
     */
    @Override
    public List<WorldRatingsDTO> getWorldRatings(Long worldId) {
        return List.of();
    }

    /**
     * 세계관 평균 별점 조회
     * @param worldId 세계관 PK
     * @return 평균 별점
     */
    @Override
    public double getWorldRatingAverage(Long worldId) {
        return 0;
    }

    /**
     * 별점 등록 (세션 완료 후 등록 가능)
     * @param worldRatingsDTO 별점 정보 (유저 PK, 세계관 PK, 세션 PK, 점수, 한줄 리뷰 포함)
     */
    @Override
    public void addWorldRating(WorldRatingsDTO worldRatingsDTO) {
    }

    /**
     * 별점 수정
     * @param id              별점 PK
     * @param worldRatingsDTO 수정할 별점 정보
     */
    @Override
    public void modifyWorldRating(Long id, WorldRatingsDTO worldRatingsDTO) {
    }

    /**
     * 별점 삭제
     * @param id 별점 PK
     */
    @Override
    public void deleteWorldRating(Long id) {
    }
}
