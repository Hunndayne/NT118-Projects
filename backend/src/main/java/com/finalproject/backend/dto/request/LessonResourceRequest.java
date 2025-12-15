package com.finalproject.backend.dto.request;

public class LessonResourceRequest {
	private String type;
	private String title;
	private String content;
	private String url;
	private String filePath;

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getUrl() {
		return url;
	}

	public String getFilePath() {
		return filePath;
	}
}
