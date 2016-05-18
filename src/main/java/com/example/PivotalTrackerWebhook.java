package com.example;

import com.example.BuildPromotionService.ArtifactorySearchResults.ArtifactorySearchResult.ArtifactorySearchResultProperties;
import com.example.artifactory.Artifactory;
import com.example.tracker.Change;
import com.example.tracker.TrackerActivityEvent;
import com.example.tracker.TrackerResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

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



@RestController
public class PivotalTrackerWebhook {

	private final Log log = LogFactory.getLog(getClass());

	private final BuildPromotionService buildPromotionService;

	@Autowired
	public PivotalTrackerWebhook(BuildPromotionService buildPromotionService) {
		this.buildPromotionService = buildPromotionService;
	}

	@RequestMapping(method = RequestMethod.POST,
			value = "/activity",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<?> onActivity(RequestEntity<TrackerActivityEvent> requestEntity) throws IOException {

		TrackerActivityEvent body = requestEntity.getBody();

		log.info("/activity webhook called: " + body);

		if (body != null) {
			/*if (body.getKind().equals("comment_create_activity")) {
				if (body.getChanges().size() > 0) {
					Change next = body.getChanges().iterator().next();
					TrackerResource resource = body.getPrimaryResources().stream().findAny().get();
					if (next != null) {
						if (next.getNewValues() != null) {
							String commitId = next.getNewValues().getCommitId();
							if (StringUtils.hasText(commitId)) {
								this.buildPromotionService.tagBuildWithPivotalTrackerStory(
										commitId, resource.getId());
							}
						}
					}
				}
			}
			*/
			if (body.getKind().equals("story_update_activity")) {
				if (body.getHighlight().equals("accepted")) {
					this.buildPromotionService.promoteAcceptedBuildToProduction(
						body.getGUID()
					);
				}
			}
		}
		return ResponseEntity.ok().build();
	}

	public void promoteAcceptedBuildToProduction(TrackerActivityEvent body) {
	}
}
