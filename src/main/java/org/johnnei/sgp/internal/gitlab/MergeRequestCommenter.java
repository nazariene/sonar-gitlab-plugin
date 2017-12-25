package org.johnnei.sgp.internal.gitlab;

import org.johnnei.sgp.internal.gitlab.api.v4.GitLabApi;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabCommit;
import org.johnnei.sgp.internal.gitlab.api.v4.model.MergeRequest;
import org.johnnei.sgp.internal.model.SonarReport;
import org.johnnei.sgp.internal.sonar.SonarReportUtil;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MergeRequestCommenter {

    private static final Logger LOGGER = Loggers.get(MergeRequestCommenter.class);

    @Nonnull
    private GitLabApi gitlabApi;

    public MergeRequestCommenter(@Nonnull GitLabApi gitlabApi) {
        this.gitlabApi = gitlabApi;
    }


    public MergeRequest process(SonarReport sonarReport) {
        MergeRequest mergeRequest = findMergeRequest(sonarReport);

        if (mergeRequest == null) {
            LOGGER.warn("Could not found related merge-request for commit. " + sonarReport.getBuildCommitSha());
            return null;
        }
        LOGGER.info("Found corresponding Merge Request for commit " + sonarReport.getBuildCommitSha() + ", MR id: " + mergeRequest.getIid());

        updateDescription(mergeRequest, sonarReport);

        mergeRequest = gitlabApi.updateMergeRequest(sonarReport.getProject().getId(), mergeRequest.getIid(), mergeRequest);

        return mergeRequest;
    }

    private MergeRequest findMergeRequest(SonarReport sonarReport) {
        //1 Get merge request ID
        LOGGER.info("Looking for opened merge requests in project " + sonarReport.getProject().getName() + " : " + sonarReport.getProject().getId());
        Collection<MergeRequest> mergeRequests = gitlabApi.getMergeRequestsInProject(sonarReport.getProject().getId(), "opened");
        mergeRequests.forEach(mergeRequest -> LOGGER.info(mergeRequest.toString()));

        final MergeRequest[] mergeRequestArr = {null};
        mergeRequests.forEach(mr -> {
            Collection<GitLabCommit> mrCommits = gitlabApi.getMergeRequestCommits(sonarReport.getProject().getId(), mr.getIid());
            mrCommits.removeIf(commit -> !commit.getId().equalsIgnoreCase(sonarReport.getBuildCommitSha()));
            if (!mrCommits.isEmpty()) {
                mergeRequestArr[0] = mr;
            }
        });

        return mergeRequestArr[0];
    }

    private void updateDescription(MergeRequest mergeRequest, SonarReport sonarReport) {
        String currentDescription = mergeRequest.getDescription();

        LOGGER.info("Previous summary: " + currentDescription);
        int currentCommentStart = currentDescription.indexOf("!" + mergeRequest.getIid());
        if (currentCommentStart == -1) {
            currentCommentStart = currentDescription.length();
        }

        String summary = "!" + mergeRequest.getIid() + " : " + SonarReportUtil.buildSummary(sonarReport);
        String newDescription = currentDescription.substring(0, currentCommentStart) + summary;

        LOGGER.info("New summary: " + newDescription);
        mergeRequest.setDescription(newDescription);
    }

    /**
     * Creates a commit comment which contains a summary of all the issues.
     *
     * @param existingComments The comments which are already there.
     * @param report           The report to comment into GitLab.
     */

  /*  private void commentSummary(List<CommitComment> existingComments, SonarReport report) {
        String summary = buildSummary(report);

        boolean hasExistingSummary = existingComments.stream()
                .filter(comment -> comment.getLine() == null)
                .anyMatch(comment -> comment.getNote().equals(summary));

        if (!hasExistingSummary) {
            try {
                LOGGER.info("Creating Merge Request summary");
                LOGGER.info(summary);
                gitlabApi.(report.getProject().getId(), report.getBuildCommitSha(), summary, null, null, null);
            } catch (IOException e) {
                throw new ProcessException("Failed to post summary comment.", e);
            }
        }
    }
*/

}
