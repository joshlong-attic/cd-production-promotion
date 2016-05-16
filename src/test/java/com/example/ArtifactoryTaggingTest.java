package com.example;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

// 6591af3b30eaf92a1023649652aa3572f90e899e
// https://github.com/joshlong/simple-cd-example/commit/6591af3b30eaf92a1023649652aa3572f90e899e
// https://travis-ci.org/joshlong/simple-cd-example/builds/130271486
// https://cloudnativejava.artifactoryonline.com/cloudnativejava/webapp/#/builds/micro-microservice/5/1463252948567/json/com.example:micro-microservice:5

// TODO
public class ArtifactoryTaggingTest {


	private final String artifactoryUsername = System.getenv("ARTIFACTORY_USERNAME"),
			artifactoryApiTokenSecret = System.getenv("ARTIFACTORY_API_TOKEN_SECRET");

	@Test
	public void testArtifactoryMetadata() throws Exception {


		this.tagBuildWithTrackerNumber(
				"cloudnativejava",
				"libs-staging-local",
				artifactoryUsername,
				artifactoryApiTokenSecret,
				"6591af3b30eaf92a1023649652aa3572f90e899e",
				"best-baruch?", "@jbaruch"
		);
	}

	RestTemplate restTemplate(String apiUsername, String token) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add("X-JFrog-Art-Api", token);
			return execution.execute(request, body);
		});
		return restTemplate;
	}


	// step 1: git commit -a -m "[fixes #123] winning."
	// this triggers PM to change the status. Meanwhile, this *might* also trigger a build
	// that lands in CF. If it does, then
	protected void tagBuildWithTrackerNumber(
			String apiRoot,
			String repositoryName,
			String apiUsername,
			String apiKey,
			String commitId,
			String key,
			String value) throws IOException {

		String api = "https://" + apiRoot + ".artifactoryonline.com/" + apiRoot;

		RestTemplate restTemplate = this.restTemplate(apiUsername, apiKey);

		String searchKey = "\"@build.vcsRevision\"";
		String searchValue = "\"" + commitId + "\"";

		String aql = String.format("items.find({ %s : %s }).include(\"name\", \"path\", \"repo\")",
				searchKey, searchValue);
		/*
			{
			  "repo" : "libs-staging-local",
			  "path" : "com/example/micro-microservice/5",
			  "name" : "micro-microservice-5.pom",
			  "type" : "file",
			  "size" : 3656,
			  "created" : "2016-05-14T19:09:58.536Z",
			  "created_by" : "admin",
			  "modified" : "2016-05-14T19:09:58.523Z",
			  "modified_by" : "admin",
			  "updated" : "2016-05-14T19:09:58.523Z"
			},
		*/
		//String aql = "items.find({\"vcsRevision\":{\"$eq\":\"" +commitId + "\"}} )";
		ResponseEntity<String> entity = restTemplate.postForEntity(api + "/api/search/aql", aql, String.class);
		String body = entity.getBody();
		System.out.println(body);

		// todo
		/**
		 * https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API
		 *
		 * 1: find the artifact info using Git commit ID:
		 *      POST /api/search/aql
		 *      BODY:
		 *          items.find({"@vcs.revision" : "5ac881f0e82ce4c8b49756cc9c60d4ffdd4eab97"}).include("name", "path", "repo")
		 *
		 * 2: then update Artifactory build to contain a Pivotal Tracker (project,  story) ID#:
		 *      http -a admin PUT https://jbaruch.artifactoryonline.com/jbaruch/api/storage/libs-releases-local/org/jfrog/test/multi/2.24/multi-2.24.pom\?properties=pt.issue=SPR-256
		 */
	}

}

