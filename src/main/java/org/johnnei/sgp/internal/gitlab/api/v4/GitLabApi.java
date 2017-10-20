package org.johnnei.sgp.internal.gitlab.api.v4;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.johnnei.sgp.internal.gitlab.api.v4.model.CommitComment;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabCommit;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabCommitComparison;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabCommitDiff;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabProject;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabSession;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitLabUser;
import org.johnnei.sgp.internal.gitlab.api.v4.model.GitlabCommitStatus;
import org.johnnei.sgp.internal.gitlab.api.v4.model.MergeRequest;
import org.johnnei.sgp.internal.gitlab.api.v4.model.MergeRequestComment;

@Path("/api/v4")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GitLabApi {

	@POST
	@Path("/session")
	GitLabSession createSession(@QueryParam("login") String login, @QueryParam("password") String password);

	@GET
	@Path("/projects/{id}")
	GitLabProject getProject(@PathParam("id") long id);

	@GET
	@Path("/projects")
	Collection<GitLabProject> getProjects(@DefaultValue("true") @QueryParam("starred") String starred);

	@GET
	@Path("/projects/{id}/repository/commits/{sha}/comments")
	List<CommitComment> getCommitComments(@PathParam("id") long id, @PathParam("sha") String commit) throws IOException;

	@POST
	@Path("/projects/{id}/repository/commits/{sha}/comments")
	void createCommitComment(@PathParam("id") int id,
		@PathParam("sha") String buildCommitSha,
		@QueryParam("note") String summary,
		@QueryParam("path") String path,
		@QueryParam("line") Integer line,
		@QueryParam("line_type") String lineType) throws IOException;

	@GET
	@Path("/projects/{id}/repository/compare")
	GitLabCommitComparison compareCommits(@PathParam("id") long id, @QueryParam("from") String baseBranch, @QueryParam("to") String commitHash) throws IOException;

	@GET
	@Path("/projects/{id}/repository/commits/{sha}/diff")
	Collection<GitLabCommitDiff> getCommitDiffs(@PathParam("id") long id, @PathParam("sha") String shortId) throws IOException;

	@POST
	@Path("/projects/{id}/statuses/{sha}")
	GitlabCommitStatus createCommitStatus(@PathParam("id") int id,
		@PathParam("sha") String buildCommitSha,
		@QueryParam("state") String status,
		@QueryParam("name") String name,
		@QueryParam("description") String description) throws IOException;

	@GET
	@Path("/user")
	GitLabUser getUser();

	@GET
	@Path("/users")
	Collection<GitLabUser> getUsers();

	@POST
	@Path("/users")
	void createUser(@QueryParam("email") String email,
		@QueryParam("password") String password,
		@QueryParam("username") String username,
		@QueryParam("name") String name,
		@QueryParam("skip_confirmation") boolean skipConfirmation);

	@PUT
	@Path("/users/{id}")
	GitLabUser updateUser(@PathParam("id") int id, @QueryParam("email") String email, @QueryParam("projects_limit") int projectsLimit);

	@POST
	@Path("/projects")
	GitLabProject createProject(@QueryParam("name") String projectName);

	@POST
	@Path("/projects/{id}/members")
	void addProjectMember(@PathParam("id") int projectId, @QueryParam("user_id") int userId, @QueryParam("access_level") int accessLevel);

	@GET
	@Path("/projects/{id}/repository/commits/{sha}/statuses")
	Collection<GitlabCommitStatus> getCommitStatuses(@PathParam("id") int project, @PathParam("sha") String commit);


	// Merge requests

	@GET
	@Path("/projects/{id}/merge_requests")
	Collection<MergeRequest> getMergeRequestsInProject(@PathParam("id") int project, @DefaultValue("all") @QueryParam("state") String state);

	@PUT
	@Path("/projects/{id}/merge_requests/{mr_id}")
	MergeRequest updateMergeRequest(@PathParam("id") int project, @PathParam("mr_id") int mergeRequestId, MergeRequest mergeRequest);

	@GET
	@Path("/projects/{id}/merge_requests/{mr_id}/commits")
	Collection<GitLabCommit> getMergeRequestCommits(@PathParam("id") int project, @PathParam("mr_id") int mergeRequestId);

	@GET
	@Path("/projects/{id}/merge_requests/{mr_id}/notes")
	Collection<MergeRequestComment> getMergeRequestComments(@PathParam("id") int project, @PathParam("mr_id") int mergeRequestId);

	@POST
	@Path("/projects/{id}/merge_requests/{mr_id}/notes")
	MergeRequestComment createMergeRequestComment(@PathParam("id") int project, @PathParam("mr_id") int mergeRequestId, @QueryParam("body") String body);
}
