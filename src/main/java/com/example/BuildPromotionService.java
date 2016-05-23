package com.example;

import com.example.BuildPromotionService.ArtifactorySearchResults.ArtifactorySearchResult.ArtifactorySearchResultProperties;
import com.example.artifactory.Artifactory;
import com.example.tracker.Tracker;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

// http://www.pivotaltracker.com/help/api/rest/v5#Resources
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

	/**
	 * this should activate blue/green deployment? how will it know which build to BG?
	 * perhaps we could have it look for the Cloud Foundry app instance that
	 * has the build ID as part of the name.
	 */
	public void promoteAcceptedBuildToProduction(String guid) {

	}

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

	private static String enquote(String w) {
		return "\"" + w + "\"";
	}

	public static class PivotalTrackerStory {
		public static class Label {
			public Long id;

			@JsonProperty(value = "created_at")
			public Date createdAt;

			@JsonProperty(value = "updated_at")
			public Date updatedAt;

			public String name;
		}
	}

	public PivotalTrackerStory.Label tagPivotalTrackerStoryWithCommitId(String projectId,
	                                                                    String storyId,
	                                                                    String commitId) {

		RestTemplate rt = this.pivotalTrackerRestTemplate;

		String uri = "https://www.pivotaltracker.com/services/v5" +
				"/projects/" + projectId +
				"/stories/" + storyId +
				"/labels";

		Map<String, String> commit_id = Collections.singletonMap("name", "commit: " + commitId);


		RequestEntity<Map<String, String>> requestEntity =
				RequestEntity
						.post(URI.create(uri))
						.body(commit_id);

		ResponseEntity<PivotalTrackerStory.Label> entity = rt.exchange(
				requestEntity,
				new ParameterizedTypeReference<PivotalTrackerStory.Label>() {
				});

		//ResponseEntity<String> exchange = rt.exchange(uri, HttpMethod.GET, null, String.class, projectId, storyId);
		PivotalTrackerStory.Label label = entity.getBody();
		log.info(ToStringBuilder.reflectionToString(label));
		log.info("storyId=" + storyId);
		log.info("projectId=" + projectId);

		return label;
	}

	public ArtifactorySearchResultProperties tagBuildWithPivotalTrackerStory(String commitId, String storyId) {

		try {

			String k = "pivotalTrackerStory";

			log.info(String.format("we want to tag build artifacts commit ID %s with %s = %s", commitId, k, (storyId)));

			Predicate<ArtifactorySearchResults.ArtifactorySearchResult> artifactorySearchResultPredicate =
					ar -> ar.name.toLowerCase().endsWith(".jar");

			String api = "https://" + artifactoryApiRoot + ".artifactoryonline.com/" + artifactoryApiRoot;

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
						restTemplate.put(urlOfArtifact + "?properties={key}={value}", null, k, (storyId));
						return restTemplate.getForEntity(urlOfArtifact + "?properties",
								ArtifactorySearchResultProperties.class).getBody();
					})
					.orElseThrow(() -> new NoSuchElementException("Couldn't find a .jar artifact."));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

