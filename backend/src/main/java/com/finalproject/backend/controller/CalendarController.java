package com.finalproject.backend.controller;

import com.finalproject.backend.dto.response.CalendarEventResponse;
import com.finalproject.backend.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

	private final CalendarService calendarService;

	@GetMapping("/events")
	public List<CalendarEventResponse> getEvents(@RequestHeader("X-Auth-Token") String token) {
		return calendarService.getEventsForCurrentUser(token);
	}
}
