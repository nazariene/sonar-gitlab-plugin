package org.johnnei.sgp.internal.gitlab.api.v4.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MergeRequest {

    private int id;

    private int iid;

    @JsonProperty("target_branch")
    private String targetBranch;

    @JsonProperty("source_branch")
    private String sourceBranch;

    @JsonProperty("project_id")
    private int projectId;

    private String title;

    private String state;

    private GitLabUser author;

    private String description;


    public int getId() {
        return id;
    }

    public int getIid() {
        return iid;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getState() {
        return state;
    }

    public GitLabUser getAuthor() {
        return author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "MergeRequest{" +
                "id=" + id +
                ", iid=" + iid +
                ", targetBranch='" + targetBranch + '\'' +
                ", sourceBranch='" + sourceBranch + '\'' +
                ", projectId=" + projectId +
                ", title='" + title + '\'' +
                ", state='" + state + '\'' +
                ", author=" + author +
                ", description='" + description + '\'' +
                '}';
    }
}
