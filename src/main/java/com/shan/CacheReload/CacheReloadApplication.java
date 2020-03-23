package com.shan.CacheReload;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
@Slf4j
public class CacheReloadApplication {
	@Value("${GOOGLE_APPLICATION_CREDENTIALS}")
	private String jsonPath;

	public static void main(String[] args) {
		SpringApplication.run(CacheReloadApplication.class, args);
	}

	@Bean
	public Datastore datatStore(){


		GoogleCredentials credentials = null;
		try {
			credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
					.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		// Create an authorized Datastore service using Application Default Credentials.
		final Datastore datastore = DatastoreOptions.newBuilder().setCredentials(credentials).build().getService();

		return datastore;

	}
}
