package com.decagon.eventhubbe.controller;

import com.decagon.eventhubbe.dto.request.EventTicketRequest;
import com.decagon.eventhubbe.dto.response.APIResponse;
import com.decagon.eventhubbe.dto.response.EventTicketResponse;
import com.decagon.eventhubbe.dto.response.TicketsSalesResponse;
import com.decagon.eventhubbe.service.EventService;
import com.decagon.eventhubbe.service.EventTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/ticket")
public class EventTicketController {
    private final EventTicketService eventTicketService;
    private final EventService eventService;

    @GetMapping("/view-event-sales/{eventId}")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<List<TicketsSalesResponse>>> trackTicket
            (@PathVariable String eventId){
        APIResponse<List<TicketsSalesResponse>> apiResponse = new APIResponse<>
                (eventTicketService.trackTicketSales(eventId));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
