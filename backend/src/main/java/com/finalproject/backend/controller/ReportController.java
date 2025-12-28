package com.finalproject.backend.controller;

import com.finalproject.backend.dto.response.ReportOverviewResponse;
import com.finalproject.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public ReportOverviewResponse getOverview(@RequestHeader("X-Auth-Token") String token) {
        return reportService.getOverview(token);
    }
}
