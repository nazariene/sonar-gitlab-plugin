package org.johnnei.sgp.internal.gitlab.api.v4.model;

public class MergeRequestComment {

    private String id;

    private String body;

    private GitLabUser author;

    private boolean system;

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public GitLabUser getAuthor() {
        return author;
    }

    public boolean isSystem() {
        return system;
    }
}
