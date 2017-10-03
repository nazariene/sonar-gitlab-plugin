package org.johnnei.sgp.internal.gitlab.api.v4.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitLabUser {

	private int id;

	private String username;

	private String name;

	private String email;

	@JsonProperty("projects_limit")
	private int projectsLimit;

	@JsonProperty("avatar_url")
	private String avatarUrl;

	@JsonProperty("web_url")
	private String webUrl;

	private String state;

	public String getUsername() {
		return username;
	}

	public int getProjectsLimit() {
		return projectsLimit;
	}

	public int getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getState() {
		return state;
	}
}
