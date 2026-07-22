package org.scoula.travel.service;

import org.scoula.travel.dto.CityCostResponseDTO;

import java.util.List;

/**
 * 여행 로드맵 서비스.
 */
public interface TravelService {

    /**
     * 도시 물가 목록을 조회. country가 null이면 전체 조회.
     */
    List<CityCostResponseDTO> findCityCosts(String country);

    /**
     * 도시 물가를 단건 조회.
     */
    CityCostResponseDTO findCityCost(Long cityCostId);
}