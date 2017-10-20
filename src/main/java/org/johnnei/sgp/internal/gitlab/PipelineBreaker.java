package org.johnnei.sgp.internal.gitlab;

import java.io.IOException;

import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;

import org.johnnei.sgp.internal.gitlab.api.v4.GitLabApi;
import org.johnnei.sgp.internal.model.MappedIssue;
import org.johnnei.sgp.internal.model.SonarReport;
import org.johnnei.sgp.internal.sonar.GitLabPluginConfiguration;

import static org.sonar.api.batch.InstantiationStrategy.PER_BATCH;

@BatchSide
@InstantiationStrategy(value = PER_BATCH)
public class PipelineBreaker {

	private final GitLabPluginConfiguration configuration;

	public PipelineBreaker(GitLabPluginConfiguration configuration) {
		this.configuration = configuration;
	}

	public void process(SonarReport report) {
		if (!configuration.isBreakPipelineEnabled()) {
			return;
		}

		String message = "";
		String status = getSuccessStatus();
		long issueCount = 0;
		if (configuration.maxBlockerIssues() >= 0 && (issueCount = countIssuesWithSeverity(report, Severity.BLOCKER)) > configuration.maxBlockerIssues()) {
			message = "Found " + issueCount + " blocker issues out of " + configuration.maxBlockerIssues() + " allowed!";
			status = getFailedStatus();
		}
		else if (configuration.maxCriticalIssues() >= 0 && (issueCount = countIssuesWithSeverity(report, Severity.CRITICAL)) > configuration.maxCriticalIssues()) {
			message = "Found " + issueCount + " critical issues out of " + configuration.maxCriticalIssues() + " allowed!";
			status = getFailedStatus();
		}
		else if (configuration.maxMajorIssues() >= 0 && (issueCount = countIssuesWithSeverity(report, Severity.MAJOR)) > configuration.maxMajorIssues()) {
			message = "Found " + issueCount + " major issues out of " + configuration.maxMajorIssues() + " allowed!";
			status = getFailedStatus();
		}
		else if (configuration.maxMinorIssues() >= 0 && (issueCount = countIssuesWithSeverity(report, Severity.MINOR)) > configuration.maxMinorIssues()) {
			message = "Found " + issueCount + " minor issues out of " + configuration.maxMinorIssues() + " allowed!";
			status = getFailedStatus();
		}

		GitLabApi gitlabAPI = configuration.createGitLabConnection();

		try {
			gitlabAPI.createCommitStatus(configuration.getProject().getId(), report.getBuildCommitSha(), status, "SonarQube", message);
		} catch (IOException e) {
			throw new ProcessException("Failed to set commit status.", e);
		}
	}

	private long countIssuesWithSeverity(SonarReport report, Severity targetSeverity) {
		return report.getIssues()
				.map(MappedIssue::getIssue)
				.map(PostJobIssue::severity).filter(severity -> severity.equals(targetSeverity))
				.count();
	}

	private String getFailedStatus() {
		return "failed";
	}

	private String getSuccessStatus() {
		return "success";
	}
}
