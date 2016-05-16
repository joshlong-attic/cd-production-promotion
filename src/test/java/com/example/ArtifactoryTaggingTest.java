package com.example;

import com.example.ArtifactoryTaggingTest.ArtifactorySearchResults.ArtifactorySearchResult;
import com.example.ArtifactoryTaggingTest.ArtifactorySearchResults.ArtifactorySearchResult.ArtifactorySearchResultProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;

// 6591af3b30eaf92a1023649652aa3572f90e899e
// https://github.com/joshlong/simple-cd-example/commit/6591af3b30eaf92a1023649652aa3572f90e899e
// https://travis-ci.org/joshlong/simple-cd-example/builds/130271486
// https://cloudnativejava.artifactoryonline.com/cloudnativejava/webapp/#/builds/micro-microservice/5/1463252948567/json/com.example:micro-microservice:5

public class ArtifactoryTaggingTest {

	private Log log = LogFactory.getLog(getClass());

	@Test
	public void testArtifactoryMetadata() throws Exception {

		String key = "best-baruch?";

		ArtifactorySearchResultProperties properties =
				this.addPropertyToBuildArtifactByCommitId(
					"cloudnativejava",
					System.getenv("ARTIFACTORY_API_TOKEN_SECRET"),
					"6591af3b30eaf92a1023649652aa3572f90e899e",
					key,
					"@jbaruch"
				);

		Map.Entry<String, Set<String>> setEntry =
				properties
						.properties
						.entrySet()
						.stream()
						.filter(e -> e.getKey().equals(key))
						.findAny()
						.get();
		Assert.assertNotNull(setEntry);
	}

	private static RestTemplate restTemplate(String token) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add("X-JFrog-Art-Api", token);
			return execution.execute(request, body);
		});
		return restTemplate;
	}

	private static String enquote(String w) {
		return "\"" + w + "\"";
	}

	private ArtifactorySearchResultProperties addPropertyToBuildArtifactByCommitId(
			String apiRoot,
			String apiKey,
			String commitId,
			String key,
			String value) throws IOException {

		String api = "https://" + apiRoot + ".artifactoryonline.com/" + apiRoot;

		RestTemplate restTemplate = restTemplate(apiKey);

		String aql = String.format("items.find({ %s : %s }).include(%s, %s, %s)",
				enquote("@build.vcsRevision"),
				enquote(commitId),
				enquote("name"),
				enquote("path"),
				enquote("repo"));

		ResponseEntity<ArtifactorySearchResults> responseEntity = restTemplate.postForEntity(api + "/api/search/aql", aql, ArtifactorySearchResults.class);
		Set<ArtifactorySearchResult> results = responseEntity.getBody().results;
		results.forEach(result -> log.info(String.format("name=%s, path=%s, repo=%s", result.name, result.path, result.repo)));
		return results
				.stream()
				.filter(ar -> ar.name.toLowerCase().endsWith(".jar")).findFirst()
				.map(jar -> {
					String urlOfArtifact = api + "/api/storage/" + jar.repo + "/" + jar.path + "/" + jar.name;
					log.info(String.format("found %s", urlOfArtifact));
					restTemplate.put(urlOfArtifact + "?properties={key}={value}", null, key, value);
					return restTemplate.getForEntity(urlOfArtifact + "?properties",
							ArtifactorySearchResultProperties.class).getBody();
				})
				.orElseThrow(() -> new NoSuchElementException("Couldn't find a .jar artifact."));
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
}