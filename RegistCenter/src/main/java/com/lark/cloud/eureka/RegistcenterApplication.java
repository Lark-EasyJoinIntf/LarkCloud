package com.lark.cloud.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class RegistcenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistcenterApplication.class, args);
	}

}

