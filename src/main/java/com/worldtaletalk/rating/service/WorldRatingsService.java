package com.worldtaletalk.rating.service;

import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import org.springframework.data.domain.Page;

public interface WorldRatingsService {

    /**
     * 세계관 별점 목록 조회 (페이징)
     *
     * @param worldId 세계관 PK
     * @return 해당 세계관의 별점 목록
     */
    Page<WorldRatingsDTO> getWorldRatings(Long worldId, int page);

    /**
     * 세계관 별점 단건 조회 (수정 폼 진입할때 필요함)
     *
     * @param ratingId 별점 PK
     * @return 해당 세계관 별점
     */
    WorldRatingsDTO getMyRatingByRatingId(Long ratingId);

    /**
     * 세계관 별점 단건 조회 (해당 세계관에 회원이 별점을 남겼는지 확인)
     *
     * @param worldId 세계관 PK
     * @param userId 회원 PK
     * @return 세계관 별점
     */
    WorldRatingsDTO getMyRatingByWorldAndUser(Long worldId, Long userId);

    /**
     * 해당 회원의 별점 목록 조회
     *
     * @param userId 회원 PK
     * @return 해당 회원 별점 목록
     */
    Page<WorldRatingsDTO> getMyRatings(Long userId, int page);

    /**
     * 세계관 평균 평점 조회
     *
     * @param worldId 세계관 PK
     * @return 평균 평점
     */
    double getWorldRatingAverage(Long worldId);

    /**
     * 별점 등록 (세션 완료 후 등록 가능)
     *
     * @param worldRatingsDTO 별점 정보
     */
    void addWorldRating(WorldRatingsDTO worldRatingsDTO);

    /**
     * 별점 및 리뷰 수정
     *
     * @param worldRatingsDTO 수정할 별점 정보
     */
    void modifyWorldRating(WorldRatingsDTO worldRatingsDTO);

    /**
     * 별점 삭제
     *
     * @param id 별점 PK
     */
    void deleteWorldRating(Long id);
}
