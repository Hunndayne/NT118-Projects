package com.example.enggo.api;

import com.google.gson.annotations.SerializedName;

public class ReportOverviewResponse {
    @SerializedName("totalStudents")
    private int totalStudents;

    @SerializedName("totalCourses")
    private int totalCourses;

    @SerializedName("totalClasses")
    private int totalClasses;

    @SerializedName("totalModules")
    private int totalModules;

    @SerializedName("submissionStatistics")
    private SubmissionStatistics submissionStatistics;

    // Getters
    public int getTotalStudents() { return totalStudents; }
    public int getTotalCourses() { return totalCourses; }
    public int getTotalClasses() { return totalClasses; }
    public int getTotalModules() { return totalModules; }
    public SubmissionStatistics getSubmissionStatistics() { return submissionStatistics; }

    // Inner class for stats
    public static class SubmissionStatistics {
        @SerializedName("onTime")
        private int onTime;

        @SerializedName("late")
        private int late;

        @SerializedName("missing")
        private int missing;

        @SerializedName("submissionRate")
        private double submissionRate;

        public int getOnTime() { return onTime; }
        public int getLate() { return late; }
        public int getMissing() { return missing; }
        public double getSubmissionRate() { return submissionRate; }
    }
}