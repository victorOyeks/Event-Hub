package com.decagon.eventhubbe.service;

import com.decagon.eventhubbe.dto.request.EventRequest;
import com.decagon.eventhubbe.dto.request.EventUpdateRequest;
import com.decagon.eventhubbe.dto.response.EventResponse;
import com.decagon.eventhubbe.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface EventService {

    EventResponse create(EventRequest request);

    EventResponse addEventBanner(String eventId, MultipartFile file);

    //deletion of event ------>
    String deleteEvent(String id);

    PageUtils searchEventsByKeyword(Integer pageNo, Integer pageSize, String sortBy, String sortDir, String keyword);

    EventResponse getEventById(String id);

    PageUtils publishEvent(Integer pageNo, Integer pageSize, String sortBy, String sortDir);

    EventResponse updateEvent(String id, EventUpdateRequest updateEvent);
}
