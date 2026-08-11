package org.scoula.travel.client;

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
        coordinates.put("인천", new Coordinate(126.7052, 37.4563));
        coordinates.put("수원", new Coordinate(127.0286, 37.2636));
        coordinates.put("성남", new Coordinate(127.1262, 37.4200));
        coordinates.put("고양", new Coordinate(126.8320, 37.6584));
        coordinates.put("용인", new Coordinate(127.1776, 37.2411));
        coordinates.put("부천", new Coordinate(126.7660, 37.5034));
        coordinates.put("안산", new Coordinate(126.8309, 37.3219));
        coordinates.put("안양", new Coordinate(126.9568, 37.3943));
        coordinates.put("평택", new Coordinate(127.1127, 36.9921));
        coordinates.put("김포", new Coordinate(126.7156, 37.6152));
        coordinates.put("의정부", new Coordinate(127.0338, 37.7381));
        coordinates.put("이천", new Coordinate(127.4350, 37.2720));
        coordinates.put("강릉", new Coordinate(128.8761, 37.7519));
        coordinates.put("춘천", new Coordinate(127.7298, 37.8813));
        coordinates.put("원주", new Coordinate(127.9202, 37.3422));
        coordinates.put("대전", new Coordinate(127.3845, 36.3504));
        coordinates.put("청주", new Coordinate(127.4890, 36.6424));
        coordinates.put("충주", new Coordinate(127.9259, 36.9910));
        coordinates.put("천안", new Coordinate(127.1522, 36.8151));
        coordinates.put("공주", new Coordinate(127.1190, 36.4465));
        coordinates.put("보령", new Coordinate(126.6128, 36.3332));
        coordinates.put("제천", new Coordinate(128.1909, 37.1326));
        coordinates.put("서산", new Coordinate(126.4503, 36.7845));
        coordinates.put("전주", new Coordinate(127.1480, 35.8242));
        coordinates.put("익산", new Coordinate(126.9578, 35.9483));
        coordinates.put("목포", new Coordinate(126.3922, 34.8118));
        coordinates.put("여수", new Coordinate(127.6622, 34.7604));
        coordinates.put("부산", new Coordinate(129.0756, 35.1796));
        coordinates.put("대구", new Coordinate(128.6014, 35.8714));
        coordinates.put("울산", new Coordinate(129.3114, 35.5384));
        coordinates.put("창원", new Coordinate(128.6811, 35.2285));
        coordinates.put("김해", new Coordinate(128.8894, 35.2285));
        coordinates.put("김천", new Coordinate(128.1136, 36.1398));
        coordinates.put("안동", new Coordinate(128.7294, 36.5684));
        coordinates.put("경주", new Coordinate(129.2247, 35.8562));
        coordinates.put("거제", new Coordinate(128.6211, 34.8806));
        coordinates.put("포항", new Coordinate(129.3650, 36.0190));
        coordinates.put("진주", new Coordinate(128.1088, 35.1799));
        coordinates.put("사천", new Coordinate(128.0642, 35.0038));
        coordinates.put("상주", new Coordinate(128.1590, 36.4109));
        coordinates.put("양산", new Coordinate(129.0372, 35.3350));
        COORDINATES = Collections.unmodifiableMap(coordinates);
    }

    private DomesticCityCoordinates() {
    }

    static Coordinate get(String city) {
        Coordinate coordinate = COORDINATES.get(city);
        if (coordinate == null) {
            throw BusinessException.badRequest(
                    "지원하지 않는 국내 출발지 또는 도착지입니다: " + city,
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
