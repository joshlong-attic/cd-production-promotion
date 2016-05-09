package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.AuthenticationManagerConfiguration;
import org.springframework.boot.autoconfigure.security.BootGlobalAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * The goal of this application is to handle all the
 * chores associated with moving work delivered and, finally, <EM>accepted</EM>
 * in Pivotal Tracker from QA to production. We'll do this using several REST API calls
 * and the {@link org.cloudfoundry.client.lib.CloudFoundryClient CloudFoundryClient}.
 *
 * @author Josh Long
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
		AuthenticationManagerConfiguration.class,
		BootGlobalAuthenticationConfiguration.class,
		OAuth2AutoConfiguration.class})
public class ProductionApplication {

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}
}

@RestController
class PivotalTrackerActivityWebhook {

	private final RestTemplate restTemplate;

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	public PivotalTrackerActivityWebhook(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/activity", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<?> onActivity(RequestEntity<TrackerActivityEvent> activity) {

		log.info("JSON: " + activity);
		TrackerActivityEvent body = activity.getBody();
		if (body != null) {
			if (body.getKind().equals("story_update_activity")) {
				if (body.getHighlight().equals("accepted")) {
					 promoteAcceptedBuildToProduction(body) ;
				}
			}
		}
		return ResponseEntity.ok().build();
	}

	protected void promoteAcceptedBuildToProduction(TrackerActivityEvent body) {

	}

}


/**
 * @apiNote this is meant to work only with v5 of the Pivotal Tracker Activity API payload schema.
 */

class TrackerActivityEvent {

	private Set<TrackerResource> primaryResources = new HashSet<>();
	private Person person;
	private Date when;
	private String kind;
	private String guid;
	private String highlight;
	private String projectVersion;
	private Project project;

	@JsonProperty("occurred_at")
	public Date getWhen() {
		return when;
	}

	@JsonProperty("primary_resources")
	public Set<TrackerResource> getPrimaryResources() {
		return this.primaryResources;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("primaryResources", primaryResources)
				.append("person", person)
				.append("when", when)
				.append("project", project)
				.append("kind", kind)
				.append("guid", guid)
				.append("highlight", highlight)
				.append("projectVersion", projectVersion)
				.toString();
	}

	public Project getProject() {
		return project;
	}

	public String getHighlight() {
		return highlight;
	}

	@JsonProperty("performed_by")
	public Person getPerson() {
		return person;
	}

	public String getKind() {
		return kind;
	}

	@JsonProperty("guid")
	public String getGUID() {
		return guid;
	}

	@JsonProperty("project_version")
	public String getProjectVersion() {
		return projectVersion;
	}
}

class Project {
	private Long id;

	private String name;

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("id", id)
				.append("name", name)
				.toString();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}

class Person {
	private String initials, name;

	private Long id;

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("initials", initials)
				.append("name", name)
				.append("id", id)
				.toString();
	}

	public String getInitials() {
		return initials;
	}

	public String getName() {
		return name;
	}

	public Long getId() {
		return id;
	}

}

class TrackerResource {
	private String kind, name, storyType;

	private URL url;

	private Long id;

	public String getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("kind", kind)
				.append("name", name)
				.append("storyType", storyType)
				.append("url", url)
				.append("id", id)
				.toString();
	}

	public String getName() {
		return name;
	}

	public Long getId() {
		return id;
	}

	@JsonProperty("story_type")
	public String getStoryType() {
		return storyType;
	}

	public URL getUrl() {
		return url;
	}

}


// This is the full JSON example
// JSON: {
//   "kind": "story_update_activity",
//   "guid": "1378566_28",
//   "project_version": 28,
//   "message": "Josh Long started this feature",
//   "highlight": "started",
//   "changes": [
//     {
//       "kind": "label",
//       "change_type": "update",
//       "id": 12099062,
//       "original_values": {
//         "counts": {
//           "number_of_zero_point_stories_by_state": {
//             "accepted": 0,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 5,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "sum_of_story_estimates_by_state": {
//             "accepted": 11,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 2,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "number_of_stories_by_state": {
//             "accepted": 5,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 7,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "kind": "story_counts"
//         }
//       },
//       "new_values": {
//         "counts": {
//           "number_of_zero_point_stories_by_state": {
//             "accepted": 0,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 5,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "sum_of_story_estimates_by_state": {
//             "accepted": 11,
//             "started": 1,
//             "finished": 0,
//             "unstarted": 1,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "number_of_stories_by_state": {
//             "accepted": 5,
//             "started": 1,
//             "finished": 0,
//             "unstarted": 6,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "kind": "story_counts"
//         }
//       },
//       "name": "admin"
//     },
//     {
//       "kind": "label",
//       "change_type": "update",
//       "id": 12099076,
//       "original_values": {
//         "counts": {
//           "number_of_zero_point_stories_by_state": {
//             "accepted": 0,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 0,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "sum_of_story_estimates_by_state": {
//             "accepted": 0,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 4,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "number_of_stories_by_state": {
//             "accepted": 0,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 3,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "kind": "story_counts"
//         }
//       },
//       "new_values": {
//         "counts": {
//           "number_of_zero_point_stories_by_state": {
//             "accepted": 0,
//             "started": 0,
//             "finished": 0,
//             "unstarted": 0,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "sum_of_story_estimates_by_state": {
//             "accepted": 0,
//             "started": 1,
//             "finished": 0,
//             "unstarted": 3,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "number_of_stories_by_state": {
//             "accepted": 0,
//             "started": 1,
//             "finished": 0,
//             "unstarted": 2,
//             "planned": 0,
//             "delivered": 0,
//             "unscheduled": 0,
//             "rejected": 0,
//             "kind": "counts_by_story_state"
//           },
//           "kind": "story_counts"
//         }
//       },
//       "name": "orders"
//     },
//     {
//       "kind": "story",
//       "change_type": "update",
//       "id": 98161134,
//       "original_values": {
//         "current_state": "unstarted",
//         "owned_by_id": null,
//         "owner_ids": [
//         ],
//         "updated_at": 1435717746000,
//         "before_id": 98161136,
//         "after_id": 98161132
//       },
//       "new_values": {
//         "current_state": "started",
//         "owned_by_id": 1721536,
//         "owner_ids": [
//           1721536
//         ],
//         "updated_at": 1462792470000,
//         "before_id": 98166508,
//         "after_id": 98161108
//       },
//       "name": "Admin can review all order questions and send responses to shoppers",
//       "story_type": "feature"
//     }
//   ],
//   "primary_resources": [
//     {
//       "kind": "story",
//       "id": 98161134,
//       "name": "Admin can review all order questions and send responses to shoppers",
//       "story_type": "feature",
//       "url": "https://www.pivotaltracker.com/story/show/98161134"
//     }
//   ],
//   "project": {
//     "kind": "project",
//     "id": 1378566,
//     "name": "My Sample Project"
//   },
//   "performed_by": {
//     "kind": "person",
//     "id": 1721536,
//     "name": "Josh Long",
//     "initials": "JL"
//   },
//   "occurred_at": 1462792470000
// }