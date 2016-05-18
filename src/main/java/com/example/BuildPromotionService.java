package com.example;

import com.example.BuildPromotionService.ArtifactorySearchResults.ArtifactorySearchResult.ArtifactorySearchResultProperties;
import com.example.artifactory.Artifactory;
import com.example.tracker.Tracker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

/**
 * TODO also, we need to make sure that when we cf push, we give each push driveb by a commit a
 * logical name that maps to that commit ID, eg: $reservation-service-{COMMIT_ID}, so that we can blue/green
 * deploy *that* specific one when the story is later accepted. We need to build in smarts to not cf push _if_ there
 * is a newer build already in production.
 *
 * @author Josh Long
 */
@Component
public class BuildPromotionService {


/*
	private RestTemplate restTemplate = new RestTemplate();
	private String token = System.getenv("PIVOTAL_TRACKER_TOKEN_SECRET");
	private String storyId = "98161154";
	private String projectId = Long.toString(1378566);
*/


	private static Log log = LogFactory.getLog(BuildPromotionService.class);

	private final String artifactoryApiRoot;

	private final RestTemplate artifactoryRestTemplate;

	private final RestTemplate pivotalTrackerRestTemplate;

	public void promoteAcceptedBuildToProduction(String trackerStoryGuid) {

		// TODO the original plan won't work because this webhook is called immediately after commit, long before
		// TODO CI has build our image for us. So, instead, well simply crawl the comments of the story from
		// TODO the tracker API. We'll need the Project ID and the X-Tracker token and the story ID.
	}


	/**
	 * Models the JSON returned from our brief interactions with JFrog Artifactory API
	 */
	public static class ArtifactorySearchResults {

		public static class ArtifactorySearchResult {

			public String repo, path, name;

			public static class ArtifactorySearchResultProperties {
				public URI uri;
				public Map<String, Set<String>> properties = new HashMap<>();
			}
		}

		public Set<ArtifactorySearchResult> results = new HashSet<>();

	}

	@Autowired
	public BuildPromotionService(
			@Artifactory RestTemplate artifactoryRestTemplate,
			@Tracker RestTemplate pivotalTrackerRestTemplate,
			@Value("${ARTIFACTORY_API_ROOT:cloudnativejava}") String artifactoryApiRoot) {
		this.artifactoryRestTemplate = artifactoryRestTemplate;
		this.artifactoryApiRoot = artifactoryApiRoot;
		this.pivotalTrackerRestTemplate = pivotalTrackerRestTemplate;
	}

	@Deprecated
	private ArtifactorySearchResultProperties addPropertyToBuildArtifactByCommitId(
			String apiRoot, String commitId,
			Predicate<ArtifactorySearchResults.ArtifactorySearchResult> artifactorySearchResultPredicate,
			String key, String value) throws IOException {

		String api = "https://" + apiRoot + ".artifactoryonline.com/" + apiRoot;

		RestTemplate restTemplate = this.artifactoryRestTemplate;

		String aql = String.format("items.find({ %s : %s }).include(%s, %s, %s)",
				enquote("@build.vcsRevision"),
				enquote(commitId),
				enquote("name"),
				enquote("path"),
				enquote("repo"));

		ResponseEntity<ArtifactorySearchResults> responseEntity =
				restTemplate.postForEntity(api + "/api/search/aql", aql, ArtifactorySearchResults.class);

		Set<ArtifactorySearchResults.ArtifactorySearchResult> results = responseEntity.getBody().results;

		results.forEach(result -> log.info(String.format(
				"name=%s, path=%s, repo=%s", result.name, result.path, result.repo)));

		return results
				.stream()
				.filter(asr -> {
					log.info("testing artifactorySearchResult " + asr.toString());
					return artifactorySearchResultPredicate.test(asr);
				}).findFirst()
				.map(jar -> {
					String urlOfArtifact = String.format("%s/api/storage/%s/%s/%s", api, jar.repo, jar.path, jar.name);
					log.info(String.format("found %s", urlOfArtifact));
					restTemplate.put(urlOfArtifact + "?properties={key}={value}", null, key, value);
					return restTemplate.getForEntity(urlOfArtifact + "?properties",
							ArtifactorySearchResultProperties.class).getBody();
				})
				.orElseThrow(() -> new NoSuchElementException("Couldn't find a .jar artifact."));
	}

	@Deprecated
	private static String enquote(String w) {
		return "\"" + w + "\"";
	}

	@Deprecated
	public ArtifactorySearchResultProperties tagBuildWithPivotalTrackerStory(String commitId, Long id) {

		try {

			String k = "pivotalTrackerStory";
			String v = Long.toString(id);

			log.info(String.format("we want to tag build artifacts commit ID %s with %s = %s", commitId, k, v));

			return addPropertyToBuildArtifactByCommitId(
					artifactoryApiRoot,
					commitId,
					ar -> ar.name.toLowerCase().endsWith(".jar"),
					k,
					v);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

