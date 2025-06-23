package com.promptdex.api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class PromptdexBackendApplication {

	// --- FIX: Add this method ---
	// This PostConstruct hook runs once after the application has been initialized.
	// It sets the default time zone for the entire Java Virtual Machine (JVM) to UTC.
	// This ensures that all date/time operations within the backend are standardized to UTC,
	// preventing time zone discrepancies regardless of where the server is hosted.
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(PromptdexBackendApplication.class, args);
	}

}