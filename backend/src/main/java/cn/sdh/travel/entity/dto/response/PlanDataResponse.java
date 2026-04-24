package cn.sdh.travel.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 结构化行程数据响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDataResponse {

    private String sessionId;
    private String destination;
    private Integer days;
    private String budget;
    private List<DayItinerary> itinerary;
    private PlanSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayItinerary {
        private Integer day;
        private String date;
        private String summary;
        private WeatherInfo weather;
        private List<Activity> activities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherInfo {
        private String condition;
        private Integer tempMin;
        private Integer tempMax;
        private String windDirection;
        private String windPower;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String id;
        private String type; // ATTRACTION, HOTEL, RESTAURANT, TRANSPORT
        private String name;
        private Location location;
        private String address;
        private String startTime;
        private String endTime;
        private String description;
        private String tips;
        private Integer cost;
        private Integer duration;
        private ActivityMetadata metadata;

        // For TRANSPORT type
        private Location from;
        private Location to;
        private String transportMode; // WALK, BUS, SUBWAY, TAXI, DRIVE
        private Integer distance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double lng;
        private Double lat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityMetadata {
        private String ticketPrice;
        private String openHours;
        private String rating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanSummary {
        private Integer totalCost;
        private String currency;
        private List<String> highlights;
    }
}
