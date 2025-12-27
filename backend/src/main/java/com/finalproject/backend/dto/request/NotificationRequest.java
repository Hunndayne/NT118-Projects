package com.finalproject.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequest {

    @NotBlank(message = "type must not be blank")
    private String type;

<<<<<<< HEAD
    @NotBlank(message = "title must not be blank")
    private String title;

    private String content;

    private Long targetUserId;

    private Long targetClassId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Long getTargetClassId() {
        return targetClassId;
    }

    public void setTargetClassId(Long targetClassId) {
        this.targetClassId = targetClassId;
    }
=======
	@NotBlank(message = "title must not be blank")
	private String title;

	private String content;

	private Long targetUserId;

	private Long targetClassId;

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public Long getTargetUserId() {
		return targetUserId;
	}

	public Long getTargetClassId() {
		return targetClassId;
	}
>>>>>>> 15646fcd7e4282cf39290213b2b470e2c7dd21be
}
