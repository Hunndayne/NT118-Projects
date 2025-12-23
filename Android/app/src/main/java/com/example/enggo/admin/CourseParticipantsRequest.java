package com.example.enggo.admin;

import java.util.List;

public class CourseParticipantsRequest {
    public List<Long> userIds;

    public CourseParticipantsRequest(List<Long> userIds) {
        this.userIds = userIds;
    }
}
