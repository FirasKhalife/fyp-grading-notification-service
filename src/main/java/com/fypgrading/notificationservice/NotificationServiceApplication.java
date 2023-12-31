package com.fypgrading.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(
		scanBasePackages = {
				"com.fypgrading.notificationservice.controller",
				"com.fypgrading.notificationservice.service",
				"com.fypgrading.notificationservice.repository",
				"com.fypgrading.notificationservice.service.mapper",
				"com.fypgrading.notificationservice.config"
		}
)
@EnableDiscoveryClient
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
