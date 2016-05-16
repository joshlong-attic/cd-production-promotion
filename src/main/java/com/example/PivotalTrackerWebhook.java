package com.example;

import com.example.tracker.Change;
import com.example.tracker.TrackerActivityEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

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
 *
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

	private final RestTemplate restTemplate;
	private Log log = LogFactory.getLog(getClass());

	@Autowired
	PivotalTrackerWebhook(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@RequestMapping(method = RequestMethod.POST,
			value = "/activity",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<?> onActivity(RequestEntity<TrackerActivityEvent> requestEntity) throws IOException {

		TrackerActivityEvent body = requestEntity.getBody();

		log.info("event: " + body);

		if (body != null) {
			if (body.getKind().equals("comment_create_activity")) {
				if (body.getChanges().size() > 0) {
					Change next = body.getChanges().iterator().next();
					if (next != null) {
						if (next.getNewValues() != null) {
							String commitId = next.getNewValues().getCommitId();
							if (StringUtils.hasText(commitId)) {
								this.tagBuildWithTrackerNumber(body);
							}
						}
					}
				}
			}

			if (body.getKind().equals("story_update_activity")) {
				if (body.getHighlight().equals("accepted")) {
					this.promoteAcceptedBuildToProduction(body);
				}
			}

		}
		return ResponseEntity.ok().build();
	}

	// step 1: git commit -a -m "[fixes #123] winning."
	// this triggers PT to change the status. Meanwhile, this *might* also trigger a build
	// that lands in CF. If it does, then
	protected void tagBuildWithTrackerNumber(TrackerActivityEvent activityEvent) {

		// todo
		/**
		 * 1: find the artifact info using Git commit ID:
		 *      items.find({"@vcs.revision" : "5ac881f0e82ce4c8b49756cc9c60d4ffdd4eab97"}).include("name", "path", "repo")
		 *
		 * 2: then update Artifactory build to contain a Pivotal Tracker (project, story) ID#:
		 *      http -a admin PUT https://jbaruch.artifactoryonline.com/jbaruch/api/storage/libs-releases-local/org/jfrog/test/multi/2.24/multi-2.24.pom\?properties=pt.issue=SPR-256
		 */
	}

	protected void promoteAcceptedBuildToProduction(TrackerActivityEvent body) {
	}
}
