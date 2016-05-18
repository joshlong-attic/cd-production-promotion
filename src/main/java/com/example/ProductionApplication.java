package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.AuthenticationManagerConfiguration;
import org.springframework.boot.autoconfigure.security.BootGlobalAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * The goal of this application is to handle all the
 * chores associated with moving work delivered and, finally, <EM>accepted</EM>
 * in Pivotal Tracker from QA to production. We'll do this using several REST API calls
 * and the {@link org.cloudfoundry.client.lib.CloudFoundryClient CloudFoundryClient}.
 *
 * @author Josh Long
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
		AuthenticationManagerConfiguration.class,
		BootGlobalAuthenticationConfiguration.class,
		OAuth2AutoConfiguration.class})
public class ProductionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}
}