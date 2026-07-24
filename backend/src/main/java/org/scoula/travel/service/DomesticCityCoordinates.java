package org.scoula.travel.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.scoula.common.exception.BusinessException;

/**
 * ODsay 경로 검색에 사용할 국내 도시 대표 좌표.
 */
final class DomesticCityCoordinates {

    private static final Map<String, Coordinate> COORDINATES;

    static {
        Map<String, Coordinate> coordinates = new HashMap<>();
        coordinates.put("서울", new Coordinate(126.9780, 37.5665));
        coordinates.put("부산", new Coordinate(129.0756, 35.1796));
        coordinates.put("대구", new Coordinate(128.6014, 35.8714));
        coordinates.put("대전", new Coordinate(127.3845, 36.3504));
        coordinates.put("강릉", new Coordinate(128.8761, 37.7519));
        coordinates.put("춘천", new Coordinate(127.7298, 37.8813));
        coordinates.put("거제", new Coordinate(128.6211, 34.8806));
        coordinates.put("안동", new Coordinate(128.7294, 36.5684));
        coordinates.put("보령", new Coordinate(126.6128, 36.3332));
        coordinates.put("창원", new Coordinate(128.6811, 35.2285));
        coordinates.put("천안", new Coordinate(127.1522, 36.8151));
        coordinates.put("청주", new Coordinate(127.4890, 36.6424));
        coordinates.put("충주", new Coordinate(127.9259, 36.9910));
        coordinates.put("안산", new Coordinate(126.8309, 37.3219));
        coordinates.put("안양", new Coordinate(126.9568, 37.3943));
        coordinates.put("부천", new Coordinate(126.7660, 37.5034));
        coordinates.put("김천", new Coordinate(128.1136, 36.1398));
        COORDINATES = Collections.unmodifiableMap(coordinates);
    }

    private DomesticCityCoordinates() {
    }

    static Coordinate get(String city) {
        Coordinate coordinate = COORDINATES.get(city);
        if (coordinate == null) {
            throw BusinessException.badRequest(
                    "ODsay 경로 검색을 위한 도시 좌표가 없습니다: " + city,
                    "TRAVEL_011");
        }
        return coordinate;
    }

    static final class Coordinate {

        private final double longitude;
        private final double latitude;

        private Coordinate(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        double getLongitude() {
            return this.longitude;
        }

        double getLatitude() {
            return this.latitude;
        }
    }
}
