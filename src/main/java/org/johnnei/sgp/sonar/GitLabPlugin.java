package org.johnnei.sgp.sonar;

import org.johnnei.sgp.internal.sonar.MergeRequestIssueJob;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;

import org.johnnei.sgp.internal.gitlab.DiffFetcher;
import org.johnnei.sgp.internal.gitlab.PipelineBreaker;
import org.johnnei.sgp.internal.sonar.CommitAnalysisBuilder;
import org.johnnei.sgp.internal.sonar.CommitIssueJob;
import org.johnnei.sgp.internal.sonar.GitLabPluginConfiguration;

/**
 * Class which configures the GitLab plugin within the SonarQube instance.
 */
@Properties({
        @Property(
                key = GitLabPlugin.GITLAB_AUTH_TOKEN,
                name = "GitLab User Token",
                description = "The private token or access token of the SonarQube user within the GitLab instance.",
                project = true,
                type = PropertyType.PASSWORD
        ),
        @Property(
                key = GitLabPlugin.GITLAB_INSTANCE_URL,
                name = "GitLab instance URL",
                description = "The URL at which the GitLab instance can be reached.",
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_PROJECT_NAME,
                name = "GitLab Project Name",
                description = "The namespace and name of the GitLab project that is being analysed.",
                project = true,
                global = false
        ),
        @Property(
                key = GitLabPlugin.GITLAB_COMMIT_HASH,
                name = "Git commit hash to analyse",
                description = "The commit which will be considered responsible for all new issues",
                global = false
        ),
        @Property(
                key = GitLabPlugin.GITLAB_BASE_BRANCH,
                name = "Git base branch",
                description = "The source branch of the analysed branch. (Ex. with GitFlow this would be develop)",
                defaultValue = "master",
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_BREAK_PIPELINE,
                name = "Break GitLab Pipeline",
                description = "If the Pipeline should break on when a critical or worse issue has been found in the incremental analysis.",
                defaultValue = "true",
                type = PropertyType.BOOLEAN,
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_MAX_BLOCKER_ISSUES,
                name = "Max blocker issues",
                description = "Amount of max blocker issues allowed",
                defaultValue = "0",
                type = PropertyType.INTEGER,
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_MAX_CRITICAL_ISSUES,
                name = "Max critical issues",
                description = "Amount of max critical issues allowed",
                defaultValue = "0",
                type = PropertyType.INTEGER,
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_MAX_MAJOR_ISSUES,
                name = "Max major issues",
                description = "Amount of max major issues allowed",
                defaultValue = "-1",
                type = PropertyType.INTEGER,
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_MAX_MINOR_ISSUES,
                name = "Max minor issues",
                description = "Amount of max minor issues allowed",
                defaultValue = "-1",
                type = PropertyType.INTEGER,
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_COMMENT_MERGE_REQUEST,
                name = "Summarize Merge Request",
                description = "Add a summary comment to Merge Request",
                defaultValue = "false",
                type = PropertyType.BOOLEAN,
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_COMMENT_COMMIT_SUMMARY,
                name = "Summarize commit",
                description = "Add a summary comment to a commit",
                defaultValue = "false",
                type = PropertyType.BOOLEAN,
                project = true
        ),
        @Property(
                key = GitLabPlugin.GITLAB_COMMENT_COMMIT_INLINE,
                name = "Inline comment commit",
                description = "Add inline comments to a commit",
                defaultValue = "false",
                type = PropertyType.BOOLEAN,
                project = true
        )
})
public class GitLabPlugin implements Plugin {

    public static final String GITLAB_INSTANCE_URL = "sonar.gitlab.uri";
    public static final String GITLAB_AUTH_TOKEN = "sonar.gitlab.auth.token";
    public static final String GITLAB_PROJECT_NAME = "sonar.gitlab.analyse.project";
    public static final String GITLAB_COMMIT_HASH = "sonar.gitlab.analyse.commit";
    public static final String GITLAB_BASE_BRANCH = "sonar.gitlab.analyse.base";
    public static final String GITLAB_BREAK_PIPELINE = "sonar.gitlab.pipeline.break";
    public static final String GITLAB_MAX_BLOCKER_ISSUES = "sonar.gitlab.pipeline.max_blocker_issues";
    public static final String GITLAB_MAX_CRITICAL_ISSUES = "sonar.gitlab.pipeline.max_critical_issues";
    public static final String GITLAB_MAX_MAJOR_ISSUES = "sonar.gitlab.pipeline.max_major_issues";
    public static final String GITLAB_MAX_MINOR_ISSUES = "sonar.gitlab.pipeline.max_minor_issues";
    public static final String GITLAB_COMMENT_MERGE_REQUEST = "sonar.gitlab.comment.mergerequest";
    public static final String GITLAB_COMMENT_COMMIT_INLINE = "sonar.gitlab.comment.commit.inline";
    public static final String GITLAB_COMMENT_COMMIT_SUMMARY = "sonar.gitlab.comment.commit.summary";

    @Override
    public void define(Context context) {
        context
                .addExtension(GitLabPluginConfiguration.class)
                .addExtension(CommitAnalysisBuilder.class)
                .addExtension(DiffFetcher.class)
                .addExtension(PipelineBreaker.class)
                .addExtension(CommitIssueJob.class)
                .addExtension(MergeRequestIssueJob.class);
    }
}
