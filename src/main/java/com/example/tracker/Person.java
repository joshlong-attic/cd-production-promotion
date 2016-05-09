package com.example.tracker;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class Person {
	private String initials, name;

	private Long id;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
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
