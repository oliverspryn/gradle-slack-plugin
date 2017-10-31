package tech.oliver.gradle.slack.models

import net.gpedro.integrations.slack.SlackAttachment
import net.gpedro.integrations.slack.SlackField
import net.gpedro.integrations.slack.SlackMessage
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.TestResult
import tech.oliver.gradle.slack.SlackPluginExtension
import tech.oliver.gradle.slack.utils.GitUtils

class SlackMessageBuilder {
    private static final String COLOR_FAILED = 'danger'
    private static final String COLOR_PASSED = 'good'
    private static final String COLOR_WARNING = 'warning'

    private static final String TASK_TITLE = 'Task'
    private static final String TASK_RESULT_TITLE = 'Task Result'
    private static final String TASK_RESULT_PASSED = 'Passed'
    private static final String TASK_RESULT_FAILED = 'Failed'
    private static final String BRANCH_TITLE = 'Git Branch'
    private static final String AUTHOR_TITLE = 'Git Author'
    private static final String COMMIT_TITLE = 'Git Commit'

    private static final String COVERAGE_REPORT = 'Unit Test Coverage Report'

    static SlackMessage coverage(SlackPluginExtension configuration, ArrayList<CoverageMetrics> metrics) {
        String color = COLOR_FAILED
        SlackMessage message
        double percentage = metrics.last().percentage // Last item is always the average

        if(configuration.calculateAverageCoverage) {
            message = new SlackMessage("${percentage}% Coverage")
        } else if (!configuration.calculateAverageCoverage && metrics.size() == 2) {
            percentage = metrics.first().percentage
            message = new SlackMessage("${percentage}% Coverage")
        } else {
            message = new SlackMessage("Coverage Report")
        }

        if (percentage > configuration.coverageWarn) color = COLOR_WARNING
        if (percentage > configuration.coverageGood) color = COLOR_PASSED

        SlackAttachment attachments = new SlackAttachment()
        attachments.setColor(color)
        attachments.setFallback('')
        attachments.setText('')

        for(def coverage : metrics) {
            if (!configuration.calculateAverageCoverage && coverage.type.toLowerCase() == "average") continue

            SlackField coverageField = new SlackField()
            coverageField.setShorten(true)
            coverageField.setTitle(coverage.type)
            coverageField.setValue("${coverage.percentage.toString()}%")
            attachments.addFields(coverageField)
        }

        message.addAttachments(attachments)
        return message
    }

    static SlackMessage coverageUnavailable(String error) {
        SlackMessage message = new SlackMessage('')

        SlackAttachment attachment = createSingleValueAttachment(
            COVERAGE_REPORT, error, COLOR_FAILED
        )

        message.addAttachments(attachment)
        return message
    }

    static SlackMessage generic(Task task, TaskState state, String taskLog) {
        Throwable failure = state.getFailure()
        StringBuilder message = new StringBuilder()
        boolean success = failure == null

        message.append(task.getDescription())

        if (!success && failure != null && failure.getCause() != null) {
            message.append('\n')
            message.append(failure.getCause())
        }

        if (!success && taskLog != null) {
            message.append('\n')
            message.append(taskLog)
        }

        String automaticMessage = message.toString()

        if (automaticMessage == null || automaticMessage.trim() == '' || automaticMessage.toLowerCase() == 'null') {
            automaticMessage = 'Gradle build finished'
        }

        SlackMessage slackMessage = new SlackMessage(automaticMessage)

        SlackAttachment attachments = new SlackAttachment()
        attachments.setColor(success ? COLOR_PASSED : COLOR_FAILED)
        attachments.setFallback('')
        attachments.setText('')

        SlackField taskField = new SlackField()
        taskField.setShorten(true)
        taskField.setTitle(TASK_TITLE)
        taskField.setValue(task.getName())
        attachments.addFields(taskField)

        SlackField resultField = new SlackField()
        resultField.setShorten(true)
        resultField.setTitle(TASK_RESULT_TITLE)
        resultField.setValue(success ? TASK_RESULT_PASSED : TASK_RESULT_FAILED)
        attachments.addFields(resultField)

        SlackField branchField = new SlackField()
        branchField.setShorten(true)
        branchField.setTitle(BRANCH_TITLE)
        branchField.setValue(GitUtils.branchName())
        attachments.addFields(branchField)

        SlackField authorField = new SlackField()
        authorField.setShorten(true)
        authorField.setTitle(AUTHOR_TITLE)
        authorField.setValue(GitUtils.lastCommitAuthor())
        attachments.addFields(authorField)

        SlackField commitField = new SlackField()
        commitField.setShorten(true)
        commitField.setTitle(COMMIT_TITLE)
        commitField.setValue(GitUtils.lastCommitMessage())
        attachments.addFields(commitField)

        slackMessage.addAttachments(attachments)

        return slackMessage
    }

    static SlackMessage summary(TestResult results) {
        boolean passed = results.failedTestCount == 0
        SlackMessage message = new SlackMessage(passed ? "All Tests Passed" : "${results.failedTestCount} Failure(s)")

        SlackAttachment attachments = new SlackAttachment()
        attachments.setColor(passed ? COLOR_PASSED : COLOR_FAILED)
        attachments.setFallback('')
        attachments.setText('')

        SlackField successField = new SlackField()
        successField.setShorten(true)
        successField.setTitle("Successful Tests")
        successField.setValue(results.successfulTestCount.toString())
        attachments.addFields(successField)

        SlackField failedField = new SlackField()
        failedField.setShorten(true)
        failedField.setTitle("Failed Tests")
        failedField.setValue(results.failedTestCount.toString())
        attachments.addFields(failedField)

        SlackField skippedField = new SlackField()
        skippedField.setShorten(true)
        skippedField.setTitle("Skipped Tests")
        skippedField.setValue(results.skippedTestCount.toString())
        attachments.addFields(skippedField)

        SlackField totalField = new SlackField()
        totalField.setShorten(true)
        totalField.setTitle("Total Tests")
        totalField.setValue(results.testCount.toString())
        attachments.addFields(totalField)

        SlackField resultField = new SlackField()
        resultField.setShorten(true)
        resultField.setTitle("Result")
        resultField.setValue(passed ? "Success" : "Failed")
        attachments.addFields(resultField)

        message.addAttachments(attachments)
        return message
    }

    private static SlackAttachment createSingleValueAttachment(String title, String value, String color) {
        SlackAttachment attachment = new SlackAttachment()
        attachment.setColor(color)
        attachment.setFallback('')
        attachment.setText('')

        SlackField report = new SlackField()
        report.setShorten(true)
        report.setTitle(title)
        report.setValue(value)

        attachment.addFields(report)
        return attachment
    }
}
