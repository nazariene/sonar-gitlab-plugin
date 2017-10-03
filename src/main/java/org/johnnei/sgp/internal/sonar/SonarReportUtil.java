package org.johnnei.sgp.internal.sonar;

import org.johnnei.sgp.internal.gitlab.MarkdownBuilder;
import org.johnnei.sgp.internal.model.SonarReport;
import org.sonar.api.batch.rule.Severity;

import java.util.Arrays;
import java.util.List;

public class SonarReportUtil {

    public static String buildSummary(SonarReport report) {
        List<Severity> severitiesInOrder = Arrays.asList(
                Severity.BLOCKER,
                Severity.CRITICAL,
                Severity.MAJOR,
                Severity.MINOR,
                Severity.INFO
        );

        MarkdownBuilder summary = new MarkdownBuilder();
        summary
                .addText(String.format("SonarQube analysis reported %d issues.", report.getIssueCount()))
                .addLineBreak();

        for (Severity severity : severitiesInOrder) {
            long count = report.countIssuesWithSeverity(severity);
            if (count == 0) {
                continue;
            }

            summary.startListItem()
                    .addSeverityIcon(severity)
                    .addText(String.format("%d %s", count, severity.name().toLowerCase()));

            report.getIssues().filter(issue -> issue.getIssue().severity().equals(severity)).forEach(issue -> summary.addLineBreak()
                    .addTab()
                    .startListItem()
                    .addText(issue.getIssue().message())
                    .addLineBreak()
                    .addText(issue.getPath()).addText(":").addText(issue.getIssue().line().toString())
                    .endListItem());

            summary.endListItem();
        }

        return summary.toString();
    }
}
