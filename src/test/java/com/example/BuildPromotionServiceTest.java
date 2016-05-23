package com.example;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

public class BuildPromotionServiceTest {

	private RestTemplate restTemplate = new RestTemplate();
	private String storyId = "98161154";
	private String projectId = Long.toString(1378566);
	private ApplicationContext applicationContext;
	private BuildPromotionService buildPromotionService;

	@Before
	public void before() throws Exception {
		this.applicationContext = SpringApplication.run(ProductionApplication.class);
		this.buildPromotionService = applicationContext.getBean(BuildPromotionService.class);
	}

	@Test
	public void testTagPivotalTrackerStoryWithCommitId()
			throws Exception {
		this.buildPromotionService.tagPivotalTrackerStoryWithCommitId(projectId, storyId, "1234567");
	}
}