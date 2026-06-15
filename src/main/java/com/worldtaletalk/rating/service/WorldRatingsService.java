package com.worldtaletalk.rating.service;

import com.worldtaletalk.rating.dto.WorldRatingsDTO;

import java.util.List;

public interface WorldRatingsService {

    /**
     * 세계관 별점 목록 조회 (페이징)
     * @param worldId 세계관 PK
     * @return 해당 세계관의 별점 목록
     */
    List<WorldRatingsDTO> getWorldRatings(Long worldId);

    /**
     * 세계관 평균 별점 조회
     * @param worldId 세계관 PK
     * @return 평균 별점
     */
    double getWorldRatingAverage(Long worldId);

    /**
     * 별점 등록 (세션 완료 후 등록 가능)
     * @param worldRatingsDTO 별점 정보 (유저 PK, 세계관 PK, 세션 PK, 점수, 한줄 리뷰 포함)
     */
    void addWorldRating(WorldRatingsDTO worldRatingsDTO);

    /**
     * 별점 수정
     * @param id              별점 PK
     * @param worldRatingsDTO 수정할 별점 정보
     */
    void modifyWorldRating(Long id, WorldRatingsDTO worldRatingsDTO);

    /**
     * 별점 삭제
     * @param id 별점 PK
     */
    void deleteWorldRating(Long id);
}
