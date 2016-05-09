package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.AuthenticationManagerConfiguration;
import org.springframework.boot.autoconfigure.security.BootGlobalAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

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

	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}



/*
export TOKEN='your Pivotal Tracker API token'
export PROJECT_ID=1378566 (My Sample Project)
curl -X GET -H "X-TrackerToken: $TOKEN" "https://www.pivotaltracker.com/services/v5/projects/$PROJECT_ID/stories/555"
*/










/*

"changes": [
  {
    "kind": "comment",
    "change_type": "create",
    "id": 133493149,
    "new_values": {
      "id": 133493149,
      "story_id": 98161154,
      "text": "Commit by Josh Long\nhttps://github.com/joshlong/simple-cd-example/commit/7ac619063898a8fe3259369a4eb6c87e097923e4\n\n[fixes #98161154] lotsa stuff",
      "person_id": 1721536,
      "created_at": 1462814822000,
      "updated_at": 1462814822000,
      "file_attachment_ids": [
      ],
      "google_attachment_ids": [
      ],
      "commit_identifier": "7ac619063898a8fe3259369a4eb6c87e097923e4",
      "commit_type": "github",
      "file_attachments": [
      ],
      "google_attachments": [
      ]
    }
  },
  {
    "kind": "story",
    "change_type": "update",
    "id": 98161154,
    "original_values": {
      "follower_ids": [
      ]
    },
    "new_values": {
      "follower_ids": [
        1721536
      ]
    },
    "name": "Signed in shopper should be able to review and remove product from favorites",
    "story_type": "feature"
  }
],

*/


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