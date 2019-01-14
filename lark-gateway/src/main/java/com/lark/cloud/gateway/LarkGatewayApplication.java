package com.lark.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
@EnableEurekaClient
@ComponentScan(basePackages = "com.lark.cloud")
public class LarkGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(LarkGatewayApplication.class, args);
	}

}

