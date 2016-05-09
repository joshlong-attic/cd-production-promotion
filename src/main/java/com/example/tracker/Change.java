package com.example.tracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class Change {

	private String kind, changeType;
	private Long id;
	private ChangeValues newValues;

	@JsonProperty("new_values")
	public ChangeValues getNewValues() {
		return newValues;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public String getKind() {
		return kind;
	}

	@JsonProperty("change_type")
	public String getChangeType() {
		return changeType;
	}

	public Long getId() {
		return id;
	}


}
