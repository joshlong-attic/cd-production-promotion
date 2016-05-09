package com.example.tracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.URL;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class TrackerResource {

	private String kind, name, storyType;
	private URL url;
	private Long id;

	public String getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
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
