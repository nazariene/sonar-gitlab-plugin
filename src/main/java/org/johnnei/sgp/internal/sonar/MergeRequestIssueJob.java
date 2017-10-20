package org.johnnei.sgp.internal.sonar;

import org.johnnei.sgp.internal.gitlab.DiffFetcher;
import org.johnnei.sgp.internal.gitlab.MergeRequestCommenter;
import org.johnnei.sgp.internal.gitlab.PipelineBreaker;
import org.johnnei.sgp.internal.model.MappedIssue;
import org.johnnei.sgp.internal.model.SonarReport;
import org.johnnei.sgp.internal.model.diff.UnifiedDiff;
import org.johnnei.sgp.internal.util.Stopwatch;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.sonar.api.batch.InstantiationStrategy.PER_BATCH;

@ScannerSide
@InstantiationStrategy(PER_BATCH)
public class MergeRequestIssueJob implements PostJob {

    private static final Logger LOGGER = Loggers.get(MergeRequestIssueJob.class);

    private static final Pattern SANATIZE_PATH_PATTERN = Pattern.compile("\\\\");

    private final GitLabPluginConfiguration configuration;

    private final DiffFetcher diffFetcher;

    private final PipelineBreaker pipelineBreaker;

    public MergeRequestIssueJob(DiffFetcher diffFetcher, GitLabPluginConfiguration configuration, PipelineBreaker pipelineBreaker) {
        this.configuration = configuration;
        this.diffFetcher = diffFetcher;
        this.pipelineBreaker = pipelineBreaker;
    }

    @Override
    public void describe(@Nonnull PostJobDescriptor postJobDescriptor) {
        postJobDescriptor.name("Comment Merge Request");
    }

    @Override
    public void execute(PostJobContext context) {
        if (!configuration.isSummarizeMergeRequestEnabled()) {
            LOGGER.info("Summarizing merge request is disabled, skipping");
            return;
        }
        LOGGER.info("Executing Merge Request Issue job");

        SonarReport report = getSonarReport(context);
        MergeRequestCommenter mrCommenter = createCommenter();
        mrCommenter.process(report);
        pipelineBreaker.process(report);
    }

    private SonarReport getSonarReport(PostJobContext context) {
        final Collection<UnifiedDiff> diffs = diffFetcher.getDiffs();
        Iterable<PostJobIssue> iterable = context.issues();
        Collection<MappedIssue> issues = StreamSupport.stream(iterable.spliterator(), false)
                .filter(PostJobIssue::isNew)
                .flatMap(issue -> mapIssueToFile(issue, diffs))
                .collect(Collectors.toList());

        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start("Creating comments in GitLab.");

        return new SonarReport.Builder()
                .setIssues(issues)
                .setBuildCommitSha(configuration.getCommitHash())
                .setProject(configuration.getProject())
                .build();
    }

    MergeRequestCommenter createCommenter() {
        return new MergeRequestCommenter(configuration.createGitLabConnection());
    }

    /**
     * Attempts to map an issue to a file in the git repository.
     * @param issue The issue to map.
     * @return The Stream containing the mapped issue or an empty stream on failure.
     */
    private Stream<MappedIssue> mapIssueToFile(PostJobIssue issue, Collection<UnifiedDiff> diffs) {
        List<UnifiedDiff> paths = findDiffByPath(issue.inputComponent(), diffs);
        if (paths.isEmpty()) {
            LOGGER.warn("Failed to find file for \"{}\" in \"{}\"", issue.message(), issue.inputComponent());
            return Stream.empty();
        }

        return findDiff(issue, paths).map(diff -> Stream.of(new MappedIssue(issue, diff, diff.getFilepath()))).orElseGet(() -> {
            LOGGER.warn("Failed to find diff for issue \"{}\" in \"{}\"", issue.message(), issue.inputComponent());
            return Stream.empty();
        });
    }

    private List<UnifiedDiff> findDiffByPath(@CheckForNull InputComponent inputComponent, Collection<UnifiedDiff> diffs) {
        if (inputComponent == null || !inputComponent.isFile()) {
            return Collections.emptyList();
        }

        InputFile inputFile = (InputFile) inputComponent;

        String issueFilePath = SANATIZE_PATH_PATTERN.matcher(inputFile.absolutePath()).replaceAll("/");
        return diffs.stream()
                .filter(diff -> issueFilePath.endsWith(diff.getFilepath()))
                .collect(Collectors.toList());
    }

    /**
     * Matches the issue to a diff.
     * @param issue The issue to match.
     * @param diffs The list of diffs in the commit.
     * @return <code>true</code> when the issue is on a modified line. Otherwise <code>false</code>.
     */
    private static Optional<UnifiedDiff> findDiff(PostJobIssue issue, Collection<UnifiedDiff> diffs) {
        Stream<UnifiedDiff> stream = diffs.stream()
                .filter(diff -> !diff.getRanges().isEmpty());

        if (issue.line() != null) {
            stream = stream.filter(diff -> diff.getRanges().stream().anyMatch(range -> range.containsLine(issue.line())));
        }

        return stream.findAny();
    }
}
