package org.scoula.travel.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.scoula.common.exception.BusinessException;

/**
 * 해외 항공권 추산에 사용할 국가별 공항 IATA 코드.
 */
final class InternationalAirportCodes {

    private static final String KOREA_DEPARTURE_AIRPORT = "ICN";
    private static final Map<String, String> COUNTRY_AIRPORT_CODES;

    static {
        Map<String, String> airportCodes = new HashMap<>();
        airportCodes.put("일본", "NRT");
        airportCodes.put("베트남", "HAN");
        COUNTRY_AIRPORT_CODES = Collections.unmodifiableMap(airportCodes);
    }

    private InternationalAirportCodes() {
    }

    static String koreaDepartureAirport() {
        return KOREA_DEPARTURE_AIRPORT;
    }

    static String destinationAirport(String country) {
        String airportCode = COUNTRY_AIRPORT_CODES.get(country);
        if (airportCode == null) {
            throw BusinessException.badRequest(
                    "지원하지 않는 해외 국가입니다: " + country,
                    "TRAVEL_016");
        }
        return airportCode;
    }
}
