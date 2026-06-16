package com.worldtaletalk.rating.service;

import com.worldtaletalk.rating.WorldRatingsMapper;
import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.repository.WorldRatingsRepository;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class WorldRatingsServiceImpl implements WorldRatingsService {
    private final WorldRatingsRepository worldRatingsRepository;
    private final WorldRatingsMapper worldRatingsMapper;
    private final StorySessionsRepository storySessionsRepository;

    /**
     * 세계관 별점 목록 조회 (페이징)
     * @param worldId 세계관 PK
     * @return 해당 세계관의 별점 목록
     */
    @Transactional(readOnly = true)
    @Override
    public Page<WorldRatingsDTO> getWorldRatings(Long worldId, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WorldRatingsEntity> worldRating = worldRatingsRepository.findAllByWorldId(worldId, pageable);
        return worldRating.map(worldRatingsMapper::toDTO);
    }

    /**
     * 세계관 평균 별점 조회
     * @param worldId 세계관 PK
     * @return 평균 별점
     */
    @Transactional(readOnly = true)
    @Override
    public double getWorldRatingAverage(Long worldId) {
        double worldRatings = worldRatingsRepository.findAverageScoreByWorldId(worldId);
        log.info("평균 평점 : {}",  worldRatings);
        return worldRatings;
    }

    /**
     * 별점 등록 (세션 완료 후 등록 가능)
     * @param worldRatingsDTO 별점 정보 (유저 PK, 세계관 PK, 세션 PK, 점수, 한줄 리뷰 포함)
     */
    @Transactional
    @Override
    public void addWorldRating(WorldRatingsDTO worldRatingsDTO) {
        StorySessionsEntity storySession = storySessionsRepository.findByIdWithUserAndWorld(worldRatingsDTO.getStorySessionId())
                .orElseThrow(() -> new NoSuchElementException("해당 세션은 존재하지 않습니다."));

        // 완결만 등록
        if (storySession.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalStateException("완결된 세션만 평점을 등록할 수 있습니다.");
        }
        if (!Objects.equals(storySession.getUsers().getId(), worldRatingsDTO.getUserId())) {
            throw new IllegalArgumentException("해당 세션의 유저가 아닙니다.");
        }

        int playCount = storySessionsRepository.countPlayNumber(
                storySession.getUsers().getId(),
                storySession.getWorlds().getId(),
                storySession.getId()
        );

        WorldRatingsEntity worldRating = WorldRatingsEntity.createWorldRating(
                worldRatingsDTO.getScore(),
                worldRatingsDTO.getReview(), // 필수값 아님 null이면 리뷰 작성 없이 평점만 작성한것
                playCount,
                storySession.getUsers(),
                storySession.getWorlds(),
                storySession
        );
        worldRatingsRepository.save(worldRating);
        log.info("평점 및 리뷰 생성 {}", worldRating);
    }

    /**
     * 평점 및 리뷰 수정
     * @param worldRatingsDTO 수정할 별점 정보
     */
    @Transactional
    @Override
    public void modifyWorldRating(WorldRatingsDTO worldRatingsDTO) {
        WorldRatingsEntity rating = worldRatingsRepository.findById(worldRatingsDTO.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 평점 및 리뷰는 존재 하지 않습니다."));

        if (!Objects.equals(rating.getUsers().getId(), worldRatingsDTO.getUserId())) {
            throw new IllegalArgumentException("해당 작성자와 다릅니다. 수정 불가");
        }

        rating.modifyInformationWorldRating(
                worldRatingsDTO.getScore() != null ? worldRatingsDTO.getScore() : rating.getScore(),
                worldRatingsDTO.getReview() != null ? worldRatingsDTO.getReview() : rating.getReview()
        );
    }

    /**
     * 별점 삭제
     * @param id 별점 PK
     */
    @Transactional
    @Override
    public void deleteWorldRating(Long id) {
        WorldRatingsEntity rating = worldRatingsRepository.findById(id).orElseThrow(() ->
            new NoSuchElementException("해당 평점 및 리뷰는 존재 하지 않습니다."));
        worldRatingsRepository.delete(rating);
        log.info("삭제 완료 : {}", rating);
    }
}
