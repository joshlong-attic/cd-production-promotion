package com.example.tracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ChangeValues {

	private Long id;
	private Long storyId;
	private String text;
	private String commitId, commitType;


	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public Long getId() {
		return id;
	}

	@JsonProperty("story_id")
	public Long getStoryId() {
		return storyId;
	}

	public String getText() {
		return text;
	}

	@JsonProperty(value = "commit_identifier")
	public String getCommitId() {
		return commitId;
	}

	@JsonProperty(value = "commit_type")
	public String getCommitType() {
		return commitType;
	}
}
