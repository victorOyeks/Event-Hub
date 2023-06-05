package com.decagon.eventhubbe;

import com.decagon.eventhubbe.domain.entities.Event;
import com.decagon.eventhubbe.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class EventHubBeApplication{

    public static void main(String[] args) {
        SpringApplication.run(EventHubBeApplication.class, args);
    }

}
