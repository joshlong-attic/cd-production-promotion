package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * The goal of this application is to handle all the
 * chores associated with moving work delivered and, finally, <EM>accepted</EM>
 * in Pivotal Tracker from QA to production. We'll do this using several REST API calls
 * and the {@link org.cloudfoundry.client.lib.CloudFoundryClient CloudFoundryClient}.
 *
 * @author Josh Long
 */
@SpringBootApplication
public class ProductionApplication {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}
}

/**
 * @apiNote  this is meant to work only with v5 of the Pivotal Tracker Activity API payload schema.
 */

@RestController
class PivotalTrackerActivityWebhook {

	private final RestTemplate restTemplate;

	@Autowired
	public PivotalTrackerActivityWebhook(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE, "*/*"})
	public void activity(@RequestBody String json) {

		System.out.println(
			"JSON: " + json
		);
	}

}