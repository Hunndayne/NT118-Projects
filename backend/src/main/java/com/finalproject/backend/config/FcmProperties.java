package com.finalproject.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fcm")
public class FcmProperties {

	private String projectId;
	private String serviceAccountPath;
	private String serviceAccountJson;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getServiceAccountPath() {
		return serviceAccountPath;
	}

	public void setServiceAccountPath(String serviceAccountPath) {
		this.serviceAccountPath = serviceAccountPath;
	}

	public String getServiceAccountJson() {
		return serviceAccountJson;
	}

	public void setServiceAccountJson(String serviceAccountJson) {
		this.serviceAccountJson = serviceAccountJson;
	}
}
