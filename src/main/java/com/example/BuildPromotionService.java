package com.example;

import com.example.artifactory.Artifactory;
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
 * @author Josh Long
 */
@Component
public class BuildPromotionService {

	private Log log = LogFactory.getLog(getClass());

	private final String apiRoot;

	private final RestTemplate artifactoryRestTemplate;

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
			@Value("${ARTIFACTORY_API_ROOT:cloudnativejava}") String apiRoot) {
		this.artifactoryRestTemplate = artifactoryRestTemplate;
		this.apiRoot = apiRoot;
	}

	/**
	 * Looks for a build artifact by a commit ID and then adds a property to that artifact.
	 *
	 * @param apiRoot
	 * @param commitId
	 * @param key
	 * @param value
	 * @return
	 * @throws IOException
	 */
	private ArtifactorySearchResults.ArtifactorySearchResult.ArtifactorySearchResultProperties addPropertyToBuildArtifactByCommitId(
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

		results.forEach(result -> log.info(String.format("name=%s, path=%s, repo=%s", result.name, result.path, result.repo)));

		//Predicate<ArtifactorySearchResults.ArtifactorySearchResult> artifactorySearchResultPredicate =;
		return results
				.stream()
				.filter(artifactorySearchResultPredicate).findFirst()
				.map(jar -> {
					String urlOfArtifact = api + "/api/storage/" + jar.repo + "/" + jar.path + "/" + jar.name;
					log.info(String.format("found %s", urlOfArtifact));
					restTemplate.put(urlOfArtifact + "?properties={key}={value}", null, key, value);
					return restTemplate.getForEntity(urlOfArtifact + "?properties",
							ArtifactorySearchResults.ArtifactorySearchResult.ArtifactorySearchResultProperties.class).getBody();
				})
				.orElseThrow(() -> new NoSuchElementException("Couldn't find a .jar artifact."));
	}

	private static String enquote(String w) {
		return "\"" + w + "\"";
	}

	public ArtifactorySearchResults.ArtifactorySearchResult.ArtifactorySearchResultProperties tagBuildWithPivotalTrackerStory(String commitId, Long id) {

		try {

			String k = "pivotalTrackerStory";
			String v = Long.toString(id);

			log.info(String.format("we want to tag build artifacts commit ID %s with %s = %s", commitId, k, v));

			return this.addPropertyToBuildArtifactByCommitId(
					apiRoot,
					commitId,
					ar -> ar.name.toLowerCase().endsWith(".jar"),
					k,
					v);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
