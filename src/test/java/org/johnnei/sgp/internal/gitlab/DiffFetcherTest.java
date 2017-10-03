package org.johnnei.sgp.internal.gitlab;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.johnnei.sgp.internal.gitlab.api.v4.GitLabApi;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabCommit;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabCommitComparison;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabCommitDiff;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabProject;
import org.johnnei.sgp.internal.model.diff.UnifiedDiff;
import org.johnnei.sgp.internal.sonar.GitLabPluginConfiguration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DiffFetcherTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private GitLabApi gitlabApiMock;

	@Mock
	private GitLabPluginConfiguration gitLabPluginConfigurationMock;

	private DiffFetcher cut;

	@Before
	public void setUp() throws Exception {
		cut = new DiffFetcher(gitLabPluginConfigurationMock);
		when(gitLabPluginConfigurationMock.createGitLabConnection()).thenReturn(gitlabApiMock);
	}

	@Test
	public void testGetDiffs() throws Exception {
		GitLabProject project = mock(GitLabProject.class);
		when(project.getId()).thenReturn(5);
		String hash = "a2b4";

		when(gitLabPluginConfigurationMock.getBaseBranch()).thenReturn("develop");
		when(gitLabPluginConfigurationMock.getCommitHash()).thenReturn(hash);
		when(gitLabPluginConfigurationMock.getProject()).thenReturn(project);

		GitLabCommit commit = mock(GitLabCommit.class);
		when(commit.getShortId()).thenReturn(hash);

		GitLabCommitComparison comparison = mock(GitLabCommitComparison.class);
		when(comparison.getCommits()).thenReturn(Collections.singletonList(commit));

		when(gitlabApiMock.compareCommits(5, "develop", hash)).thenReturn(comparison);

		String diff = "--- a/src/Main.java\n" +
			"+++ b/src/Main.java\n" +
			"@@ -1,5 +1,5 @@\n" +
			"-package org.johnnei.sgp;\n" +
			"+package org.johnnei.sgp.it;\n" +
			" \n" +
			" import java.io.IOException;\n" +
			" import java.nio.file.Files;";

		GitLabCommitDiff commitDiff = mock(GitLabCommitDiff.class);
		when(commitDiff.getDeletedFile()).thenReturn(false);
		when(commitDiff.getDiff()).thenReturn(diff);

		when(gitlabApiMock.getCommitDiffs(5, hash)).thenReturn(Collections.singletonList(commitDiff));

		List<UnifiedDiff> diffs = (List<UnifiedDiff>) cut.getDiffs();

		assertThat("Diff should have been returned.", diffs, hasSize(1));
		assertThat("Incorrect hash for diff", diffs.get(0).getCommitSha(), equalTo(hash));
	}

	@Test
	public void testGetDiffsExceptionOnCompare() throws Exception {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("compare");
		thrown.expectCause(isA(IOException.class));

		GitLabProject project = mock(GitLabProject.class);
		when(project.getId()).thenReturn(5);
		String hash = "a2b4";

		when(gitLabPluginConfigurationMock.getBaseBranch()).thenReturn("develop");
		when(gitLabPluginConfigurationMock.getCommitHash()).thenReturn(hash);
		when(gitLabPluginConfigurationMock.getProject()).thenReturn(project);

		when(gitlabApiMock.compareCommits(5, "develop", hash)).thenThrow(new IOException("Test Exception path"));

		cut.getDiffs();
	}

	@Test
	public void testGetDiffsExceptionOnGetDiff() throws Exception {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("diff");
		thrown.expectCause(isA(IOException.class));

		GitLabProject project = mock(GitLabProject.class);
		when(project.getId()).thenReturn(5);
		String hash = "a2b4";

		when(gitLabPluginConfigurationMock.getBaseBranch()).thenReturn("develop");
		when(gitLabPluginConfigurationMock.getCommitHash()).thenReturn(hash);
		when(gitLabPluginConfigurationMock.getProject()).thenReturn(project);

		GitLabCommit commit = mock(GitLabCommit.class);
		when(commit.getShortId()).thenReturn(hash);

		GitLabCommitComparison comparison = mock(GitLabCommitComparison.class);
		when(comparison.getCommits()).thenReturn(Collections.singletonList(commit));

		when(gitlabApiMock.compareCommits(5, "develop", hash)).thenReturn(comparison);
		when(gitlabApiMock.getCommitDiffs(5, hash)).thenThrow(new IOException("Test Exception path"));

		cut.getDiffs();
	}

}
