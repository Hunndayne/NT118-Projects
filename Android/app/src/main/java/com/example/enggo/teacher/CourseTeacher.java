package com.example.enggo.teacher;

public class CourseTeacher {
    private long id;
    private String name;
    private String classCode;
    private int lessonCount;

    public CourseTeacher(long id, String name, String classCode, int lessonCount) {
        this.id = id;
        this.name = name;
        this.classCode = classCode;
        this.lessonCount = lessonCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getClassCode() {
        return classCode;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }
}
