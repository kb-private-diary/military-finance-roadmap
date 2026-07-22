package org.scoula.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.travel.domain.CityCostVO;
import org.scoula.travel.dto.CityCostResponseDTO;
import org.scoula.travel.mapper.TravelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {

    private final TravelMapper mapper;

    @Override
    public List<CityCostResponseDTO> findCityCosts(String country) {
        log.info("findCityCosts: country = " + country);
        return this.mapper.getCityCostList(country).stream()
                .map(CityCostResponseDTO::of)
                .collect(Collectors.toList());
    }

    @Override
    public CityCostResponseDTO findCityCost(Long cityCostId) {
        log.info("findCityCost: cityCostId = " + cityCostId);
        CityCostVO vo = Optional.ofNullable(this.mapper.getCityCost(cityCostId))
                .orElseThrow(() -> new NoSuchElementException(
                        "존재하지 않는 도시 물가입니다. cityCostId = " + cityCostId));
        return CityCostResponseDTO.of(vo);
    }
}