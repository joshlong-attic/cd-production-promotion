package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class PivotalTrackerStoryApiTest {

	private RestTemplate restTemplate = new RestTemplate();
	private String token = System.getenv("PIVOTAL_TRACKER_TOKEN_SECRET");
	private String storyId = "98161154";
	private String projectId = Long.toString(1378566);
	private Log log = LogFactory.getLog(getClass());


	// https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API
	// for more on sending requests using the Artifactory API key

	private static void configureXTrackerTokenInterceptor(RestTemplate restTemplate, String token) {
		restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add("X-TrackerToken", token);
			return execution.execute(request, body);
		});
	}

	@Before
	public void before() throws Exception {
		configureXTrackerTokenInterceptor(this.restTemplate, this.token);
	}

	@Test
	public void testStoriesByProjectAndStoryId() throws Exception {
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(
				"https://www.pivotaltracker.com/services/v5/projects/{projectId}/stories/{storyId}",
				HttpMethod.GET,
				null, String.class, this.projectId, this.storyId
		);

		this.log.info("response: " + responseEntity.getBody());

	}
}

class PivotalTrackerStoryApi {


}