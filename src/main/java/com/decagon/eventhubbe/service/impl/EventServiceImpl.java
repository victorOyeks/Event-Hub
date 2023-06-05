package com.decagon.eventhubbe.service.impl;

import com.decagon.eventhubbe.ENUM.EventCategory;
import com.decagon.eventhubbe.config.CloudinaryConfig;
import com.decagon.eventhubbe.domain.entities.AppUser;
import com.decagon.eventhubbe.domain.entities.Event;
import com.decagon.eventhubbe.domain.entities.EventTicket;
import com.decagon.eventhubbe.domain.entities.geoLocation.GeoResponse;
import com.decagon.eventhubbe.domain.repository.AccountRepository;
import com.decagon.eventhubbe.domain.repository.EventRepository;
import com.decagon.eventhubbe.domain.repository.EventTicketRepository;
import com.decagon.eventhubbe.dto.request.EventRequest;
import com.decagon.eventhubbe.dto.request.EventTicketRequest;
import com.decagon.eventhubbe.dto.request.EventUpdateRequest;
import com.decagon.eventhubbe.dto.response.EventResponse;
import com.decagon.eventhubbe.exception.EventNotFoundException;
import com.decagon.eventhubbe.exception.UnauthorizedException;
import com.decagon.eventhubbe.service.EventService;
import com.decagon.eventhubbe.utils.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.geo.Point;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.decagon.eventhubbe.utils.LocationUtils.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {


    private final EventRepository eventRepository;
    private final EventTicketRepository eventTicketRepository;
    private final AppUserServiceImpl appUserService;
    private final MongoTemplate mongoTemplate;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @Override
    public EventResponse create(EventRequest request) {
        AppUser user = appUserService.getUserByEmail(UserUtils.getUserEmailFromContext());

        GeoResponse geoDetails = getGeoDetails(request);
        String actualLocation = extractActualLocation(geoDetails);
        Point pointLocation = convertToCoordinates(geoDetails);

        if(!accountRepository.existsByAppUser(user)){
            throw new RuntimeException("USER NEED TO UPDATE HIS PROFILE");
        }
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .caption(request.getCaption())
                .appUser(user)
                .category(EventCategory.fromDisplayName(request.getCategory()))
                .location(actualLocation)
                .pointLocation(pointLocation)
                .organizer(request.getOrganizer())
                .isDeleted(false)
                .startDate(request.getStartDate())
                .startTime(request.getStartTime())
                .endDate(request.getEndDate())
                .endTime(request.getEndTime())
                .createdAt(DateUtils.saveDate(LocalDateTime.now()))
                .build();
        Event savedEvent = eventRepository.save(event);
        List<EventTicketRequest> ticketRequest = request.getTickets();
        ticketRequest.forEach(ticket -> eventTicketRepository.save(
                EventTicket.builder()
                        .event(savedEvent)
                        .description(ticket.getDescription())
                        .quantity(ticket.getQuantity())
                        .ticketClass(ticket.getTicketClass())
                        .ticketPrice(ticket.getTicketPrice())
                        .build()
        ));
        savedEvent.setEventTickets(eventTicketRepository.findAllByEvent(savedEvent));
        return modelMapper.map(eventRepository.save(savedEvent), EventResponse.class);
    }
    @Override
    public EventResponse addEventBanner(String eventId, MultipartFile file){
        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(eventId));
        CloudinaryConfig cloudinaryConfig = new CloudinaryConfig();
        event.setBannerUrl(cloudinaryConfig.imageLink(file,event.getId()));
        Event savedEvent = eventRepository.save(event);
        return modelMapper.map(eventRepository.save(savedEvent), EventResponse.class);
    }

    // Implementing the deletion of Event ----->
    @Override
    public String deleteEvent(String id) {

        Event eventToDelete = eventRepository
                .findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));
        AppUser appUser = appUserService.getUserByEmail(UserUtils.getUserEmailFromContext());
        if(!eventToDelete.getAppUser().equals(appUser)){
            throw new UnauthorizedException("Unauthorized! Event created by another user");
        }
        eventToDelete.setDeleted(true);
        eventRepository.save(eventToDelete);
        return "Event with title : "+eventToDelete.getTitle()+" deleted successfully";
    }
    @Override
    public PageUtils searchEventsByKeyword(Integer pageNo, Integer pageSize, String sortBy, String sortDir, String keyword) {
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matchingAny(keyword);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize,sort);

        TextQuery textQuery = TextQuery.queryText(textCriteria)
                .sortByScore();
        Pair<List<Event>, Long> result = executeQuery(textQuery, pageable);

        Page<Event> eventPage = PageableExecutionUtils.getPage(result.getFirst(), pageable, result::getSecond);
        List<Event> events = eventPage.getContent();
        if(events.isEmpty()){
            throw  new EventNotFoundException(" search word is not found");
        }
        List<EventResponse> eventResponseList = events.stream()
                .filter(EventUtils::eventValidation)
                .map(event -> modelMapper.map(event, EventResponse.class))
                .toList();

        return PageUtils.builder()
                .content(eventResponseList)
                .pageNo(eventPage.getNumber())
                .pageSize(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .totalPage(eventPage.getTotalPages())
                .isLast(eventPage.isLast())
                .build();

    }
    private Pair<List<Event>, Long> executeQuery(TextQuery query, Pageable pageable) {
        Assert.notNull(query, "TextQuery must not be null");
        Assert.notNull(pageable, "Pageable must not be null");

        query.with(pageable);

        List<Event> results = mongoTemplate.find(query, Event.class);
        long count = calculateCount(query);

        return Pair.of(results, count);
    }

    private long calculateCount(TextQuery query) {
        return mongoTemplate.count(query, Event.class);
    }

    @Override
    public EventResponse getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        return modelMapper.map(event, EventResponse.class);

    }

    @Override
    public PageUtils publishEvent(Integer pageNo, Integer pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Event> eventPage = eventRepository.findAll(PageRequest.of(pageNo, pageSize, sort));
        List<Event> events = new ArrayList<>();

        eventPage.getContent().forEach(event -> {
            if (EventUtils.eventValidation(event)) {
                events.add(event);
            }
        });
        System.out.println(events.get(0).getCaption());
        List<EventResponse> eventResponses = events.stream().map(event -> modelMapper.map(event, EventResponse.class))
                .collect(Collectors.toList());
        return PageUtils.builder()
                        .content(eventResponses)
                        .pageNo(eventPage.getNumber())
                        .pageSize(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .totalPage(eventPage.getTotalPages())
                .isLast(eventPage.isLast())
                .build();

    }
    @Override
    public EventResponse updateEvent(String eventId, EventUpdateRequest updateEvent) {
        AppUser appUser = appUserService.getUserByEmail(UserUtils.getUserEmailFromContext());
        GeoResponse geoDetails = getGeoDetails(updateEvent);
        String actualLocation = extractActualLocation(geoDetails);
        Point pointLocation = convertToCoordinates(geoDetails);
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        if(!eventToUpdate.getAppUser().equals(appUser)){
            throw new UnauthorizedException("Event Created By Another User");
        }
        eventToUpdate.setTitle(updateEvent.getTitle());
        eventToUpdate.setCaption(updateEvent.getCaption());
        eventToUpdate.setDescription(updateEvent.getDescription());
        eventToUpdate.setOrganizer(updateEvent.getOrganizer());
        eventToUpdate.setCategory(EventCategory.valueOf(updateEvent.getCategory()));
        eventToUpdate.setLocation(actualLocation);
        eventToUpdate.setPointLocation(pointLocation);
        eventToUpdate.setStartDate(updateEvent.getStartDate());
        eventToUpdate.setEndDate(updateEvent.getEndDate());
        eventToUpdate.setStartTime(updateEvent.getStartTime());
        eventToUpdate.setEndTime(updateEvent.getEndTime());
        return modelMapper.map(eventRepository.save(eventToUpdate), EventResponse.class);
    }
}


