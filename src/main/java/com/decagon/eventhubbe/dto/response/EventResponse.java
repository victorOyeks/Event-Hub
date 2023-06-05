package com.decagon.eventhubbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventResponse {
    private String id;
    private String title;
    private String caption;
    private String description;
    private String bannerUrl;
    private String organizer;
    private String category;
    private Point pointLocation;
    private String location;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private List<EventTicketResponse> tickets;
}
