package com.catcher.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class })
@ComponentScan(basePackages = {"com.catcher.core", "com.catcher.resource", "com.catcher.infrastructure", "com.catcher.datasource", "com.catcher.common", "com.catcher.config", "com.catcher.security"})
@EnableJpaRepositories(basePackages = {"com.catcher.datasource"})
@EntityScan(basePackages = {"com.catcher.core.domain.entity"})
@EnableFeignClients(basePackages = "com.catcher.resource.external")
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}
}
