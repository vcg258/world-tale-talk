package com.worldtaletalk.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldPersonaPurgeJdbcService {
    private NamedParameterJdbcTemplate jdbc;

    public void purgWorld(Long worldId) {
        Map<String, Object> world = jdbc.queryForMap("""
                SELECT * FROM worlds WHERE id = :worldId
                """, new MapSqlParameterSource("worldsId", worldId));

    }
}
