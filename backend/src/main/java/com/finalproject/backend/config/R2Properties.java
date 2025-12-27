package com.finalproject.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "r2")
@Getter
@Setter
public class R2Properties {

	private String accountId;
	private String accessKeyId;
	private String secretAccessKey;
	private String bucket;
	private String publicBaseUrl;
	private Duration presignDuration = Duration.ofMinutes(10);

	public boolean isConfigured() {
		return isNotBlank(accountId)
				&& isNotBlank(accessKeyId)
				&& isNotBlank(secretAccessKey)
				&& isNotBlank(bucket);
	}

	public String resolveEndpoint() {
		if (!isNotBlank(accountId)) {
			return null;
		}
		return "https://" + accountId.trim() + ".r2.cloudflarestorage.com";
	}

	public String resolvePublicBaseUrl() {
		if (isNotBlank(publicBaseUrl)) {
			return stripTrailingSlash(publicBaseUrl.trim());
		}
		if (isNotBlank(bucket) && isNotBlank(accountId)) {
			return "https://" + bucket.trim() + "." + accountId.trim() + ".r2.dev";
		}
		return null;
	}

	private static boolean isNotBlank(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static String stripTrailingSlash(String value) {
		while (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}
}
