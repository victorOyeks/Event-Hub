package com.decagon.eventhubbe.domain.entities;

import com.decagon.eventhubbe.ENUM.EventCategory;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "events")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@CompoundIndexes({
        @CompoundIndex(name = "event_text_index", def = "{'title': 'text', 'description': 'text', 'caption': 'text'}, default_language='english', weights = {'title': 3, 'description': 2, 'caption': 1}")
})
public class Event {

    @Id
    private String id;
    private String title;
    private String caption;
    private String description;
    private String organizer;
    @Field("category")
    private EventCategory category;
    private Point pointLocation;
    private String location;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String bannerUrl;
    private String createdAt;
    private LocalDateTime date;
    private boolean isDeleted;

    @DBRef
    private AppUser appUser;

    @DBRef
    private List<EventTicket> eventTickets;

}
