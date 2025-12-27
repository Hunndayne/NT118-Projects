package com.finalproject.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class R2Config {

	private final R2Properties r2Properties;

	@Bean
	@ConditionalOnProperty(prefix = "r2", name = {"account-id", "access-key-id", "secret-access-key", "bucket"})
	public S3Presigner r2Presigner() {
		String endpoint = r2Properties.resolveEndpoint();
		AwsBasicCredentials credentials = AwsBasicCredentials.create(
				r2Properties.getAccessKeyId().trim(),
				r2Properties.getSecretAccessKey().trim()
		);

		return S3Presigner.builder()
				.endpointOverride(URI.create(endpoint))
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.region(Region.US_EAST_1)
				.serviceConfiguration(S3Configuration.builder()
						.pathStyleAccessEnabled(true)
						.build())
				.build();
	}
}
