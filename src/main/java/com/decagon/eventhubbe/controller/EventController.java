package com.decagon.eventhubbe.controller;

import com.decagon.eventhubbe.dto.request.EventRequest;
import com.decagon.eventhubbe.dto.request.EventUpdateRequest;
import com.decagon.eventhubbe.dto.response.APIResponse;
import com.decagon.eventhubbe.dto.response.EventResponse;
import com.decagon.eventhubbe.service.EventService;
import com.decagon.eventhubbe.utils.PageConstant;
import com.decagon.eventhubbe.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<EventResponse>> createEvent(@RequestBody EventRequest eventRequest) {
        APIResponse<EventResponse> response = new APIResponse<>(eventService.create(eventRequest));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping(value = "/create/{eventId}/event-banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<EventResponse>> addEventBanner(@PathVariable String eventId,
            @RequestParam("file") MultipartFile file){
        APIResponse<EventResponse> response = new APIResponse<>(eventService.addEventBanner(eventId,file));
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @GetMapping("/view-event/")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<PageUtils>> findAllEvents(
            @RequestParam(value = "pageNo", defaultValue = PageConstant.pageNo) Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = PageConstant.pageSize) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = PageConstant.sortBy) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PageConstant.sortDir) String sortDir) {

        APIResponse<PageUtils> apiResponse = new APIResponse<>(eventService.publishEvent(pageNo, pageSize, sortBy, sortDir));
        return ResponseEntity.ok().body(apiResponse);
    }
    @GetMapping("/search-event")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<PageUtils>> findAllEventsByKeyword(
            @RequestParam(value = "pageNo", defaultValue = PageConstant.pageNo) Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = PageConstant.pageSize) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = PageConstant.sortBy) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PageConstant.sortDir) String sortDir,
            @RequestParam("keyword") String keyword) {

        APIResponse<PageUtils> apiResponse = new APIResponse<>(eventService.searchEventsByKeyword(
                pageNo, pageSize, sortBy, sortDir, keyword));
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping("/view-event/{eventId}")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<EventResponse>> getEventById(
            @PathVariable String eventId){
        APIResponse<EventResponse> apiResponse = new APIResponse<>(eventService.getEventById(eventId));
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{eventId}")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<EventResponse>> updateEvent(
            @PathVariable String eventId,
            @RequestBody EventUpdateRequest updateEvent
    ){
        APIResponse<EventResponse> apiResponse = new APIResponse<>(eventService.updateEvent(eventId, updateEvent));
        return new ResponseEntity<>(
                apiResponse, HttpStatus.OK
        );
    }

    // Implementing the deletion of Event ----->
    @DeleteMapping("/{eventId}")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<String>> deleteEvent(
            @PathVariable String eventId) {
        APIResponse<String> apiResponse = new APIResponse<>(eventService.deleteEvent(eventId));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
