package com.taco1.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Getter
@Setter
public class MetadataDTO {

    // 생성 시간
    private String creationTime;

    // 시간대 정보
    private String timeZone;

    //기초 위경도
    private Location location;

    // exif
    private Map<String, Object> exif;

    @Getter
    @Setter
    // 내부 클래스로 위치 정보 표현
    public static class Location {
        private Double latitude;
        private Double longitude;

    }

    // 생성 시간과 시간대 정보를 기반으로 LocalDateTime 객체를 반환
    public LocalDateTime getCreationTimeInClientZone() {
        if (creationTime == null || timeZone == null) return null;
        try {
            Instant instant = Instant.parse(creationTime); // UTC 기준
            ZoneId zoneId = ZoneId.of(timeZone);
            return LocalDateTime.ofInstant(instant, zoneId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // exif 위도 추출
    public Double getExifLatitude() {
        if (exif == null) return null;
        Object lat = exif.get("GPSLatitude");
        return (lat instanceof Number) ? ((Number) lat).doubleValue() : null;
    }

    // exif 경도 추출
    public Double getExifLongitude() {
        if (exif == null) return null;
        Object lon = exif.get("GPSLongitude");
        return (lon instanceof Number) ? ((Number) lon).doubleValue() : null;
    }

    //exif 날짜 추출
    public LocalDateTime getExifDateTimeOriginalAsDateTime() {
        if (exif == null) return null;
        Object dateObj = exif.get("DateTimeOriginal");

        if (dateObj instanceof String) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
                return LocalDateTime.parse((String) dateObj, formatter);
            } catch (DateTimeParseException e) {
                // 예외 발생 시 null 반환
                System.err.println("EXIF 날짜 파싱 실패: " + dateObj);
            }
        }

        return null;
    }

}
