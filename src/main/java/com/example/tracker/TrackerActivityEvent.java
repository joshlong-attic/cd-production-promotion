package com.example.tracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class TrackerActivityEvent {

	private Person person;
	private Project project;
	private Date when;

	private String kind;
	private String guid;
	private String highlight;
	private String projectVersion;

	private Set<TrackerResource> primaryResources = new HashSet<>();
	private Set<Change> changes = new HashSet<>();

	public Set<Change> getChanges() {
		return changes;
	}

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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
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
