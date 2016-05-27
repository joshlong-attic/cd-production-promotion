package com.example;

import com.example.artifactory.Artifactory;
import com.example.bintray.Bintray;
import com.example.tracker.Tracker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/***
 * WORKFLOW :
 * <p>
 * <p>
 * you commit something.
 * it should contain the tokens that GitHub + Pivotal Tracker post commit
 * hooking is looking for in order for it to trigger https://www.pivotaltracker.com/help/api#Tracker_Updates_in_SCM_Post_Commit_Hooks.
 * This associates a commit identifier with the ticket. You *should* be able to then get that in the
 * promote to production thing. Which you'll have as metadata on a given build. Assuming that build is in
 * Artifactory, it'll also be promoted to bintray and green/blue deployed to CF. The trick is to
 * avoid the BG deploy if the current staging environment doesnt have the build you're looking for.
 * Maybe each run of the integration could create a NEW cf push? But then we'd have to delete all
 * the damned apps because each commit would trigger a new one that could eventually become production? UGH.
 * <p>
 * <p>
 * use this to set a custom property
 * <p>
 * {@code PUT /api/storage/libs-release-local/ch/qos/logback/logback-classic/0.9.9?properties=os=win,linux|qa=done&recursive=1 }
 */


/**
 * - ill have two webhooks from PT.
 * - the first when the developer does git commit -a -m “FIXES #1234”
 * - PT will mark the story as ‘delivered’ and it’ll pass in the  PT # and the commit ID then in the web hook as a comment
 * - later, after product management has clicked around and ‘accepted’ the change, she’ll click ‘Deliver’ in PT. Ill have the PT# there.
 * - but not the commit ID
 * - so i want to be able to use the PT# to find in Artifactory the latest build
 */

// TODO make it so that whe we get an activity update for something being delivered, we correllate


/*
	<groupId>com.example</groupId>
	<artifactId>micro-microservice</artifactId>

	export TOKEN='your Pivotal Tracker API token'
	export PROJECT_ID=1378566 (My Sample Project)
	curl -X GET -H "X-TrackerToken: $TOKEN" "https://www.pivotaltracker.com/services/v5/projects/$PROJECT_ID/stories/555"
*/



@Configuration
public class BuildPromotionConfiguration {

	@Bean
	@Bintray
	RestTemplate bintrayRestTemplate(String user, String pw) {
		RestTemplate r = new RestTemplate();
		r.getInterceptors().add((request, body, execution) -> {
			String token = Base64Utils.encodeToString((user + ":" + pw)
					.getBytes(Charset.forName("UTF-8")));
			request.getHeaders().add("Authorization", "Basic " + token);
			return execution.execute(request, body);
		});
		return r;
	}

	@Bean
	@Tracker
	RestTemplate trackerRestTemplate(
			@Value("${PIVOTAL_TRACKER_TOKEN_SECRET}") String token) {
		return authenticatedRestTemplate("X-TrackerToken", token);
	}

	@Bean
	@Artifactory
	RestTemplate restTemplate(
			@Value("${ARTIFACTORY_API_TOKEN_SECRET}") String token) {
		return authenticatedRestTemplate("X-JFrog-Art-Api", token);
	}

	private RestTemplate authenticatedRestTemplate(String h, String v) {
		RestTemplate r = new RestTemplate();
		r.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add(h, v);
			return execution.execute(request, body);
		});
		return r;
	}
}

