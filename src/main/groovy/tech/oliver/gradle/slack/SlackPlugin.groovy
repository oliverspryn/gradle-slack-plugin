package tech.oliver.gradle.slack

import net.gpedro.integrations.slack.SlackApi
import net.gpedro.integrations.slack.SlackMessage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.logging.StandardOutputListener
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.testing.jacoco.tasks.JacocoReport
import tech.oliver.gradle.slack.models.ConsoleMessageBuilder
import tech.oliver.gradle.slack.models.CoverageMetrics
import tech.oliver.gradle.slack.models.SlackMessageBuilder

class SlackPlugin implements Plugin<Project> {

    SlackPluginExtension mExtension
    StringBuilder mTaskLogBuilder
    TestResult savedTestResults

    SlackPlugin() {
        savedTestResults = null
    }

    void apply(Project project) {
        mTaskLogBuilder = new StringBuilder()
        mExtension = project.extensions.create('slack', SlackPluginExtension)

        project.afterEvaluate {
            if (mExtension.url != null && mExtension.enabled)
                monitorTasksLifecycle(project)
        }
    }

    void monitorTasksLifecycle(Project project) {
        project.getGradle().getTaskGraph().addTaskExecutionListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                task.logging.addStandardOutputListener(new StandardOutputListener() {
                    @Override
                    void onOutput(CharSequence charSequence) {
                        mTaskLogBuilder.append(charSequence)
                    }
                })

                if (task instanceof Test) {
                    Test test = task as Test

                    test.addTestListener(new TestListener() {
                        @Override
                        void beforeSuite(TestDescriptor testDescriptor) { }

                        @Override
                        void afterSuite(TestDescriptor testDescriptor, TestResult testResult) {
                            if (!testDescriptor.parent) // Matches the outer most suite... aka show final summary
                                savedTestResults = testResult
                        }

                        @Override
                        void beforeTest(TestDescriptor testDescriptor) { }

                        @Override
                        void afterTest(TestDescriptor testDescriptor, TestResult testResult) { }
                    })
                }
            }

            @Override
            void afterExecute(Task task, TaskState state) {
                handleTaskFinished(task, state)
            }
        })
    }

    void handleTaskFinished(Task task, TaskState state) {
        Throwable failure = state.getFailure()
        boolean shouldSendMessage = failure != null || shouldMonitorTask(task)

        // only send a slack message if the task failed
        // or the task is registered to be monitored
        if (shouldSendMessage) {
            SlackApi api = new SlackApi(mExtension.url)
            SlackMessage message

            if (task instanceof JacocoReport) { // Coverage
                JacocoReport jacoco = task as JacocoReport

                try {
                    if (mExtension.showCoverage) {
                        def metrics = calculateCoverageMetrics(jacoco)
                        message = SlackMessageBuilder.coverage(mExtension, metrics)

                        if (mExtension.showConsoleReports)
                            ConsoleMessageBuilder.coverage(mExtension, metrics)
                    }
                } catch (Exception ex) {
                    message = SlackMessageBuilder.coverageUnavailable(ex.message)
                }
            } else if (task instanceof Test) { // Unit test summary
                if (mExtension.showUnitTest) {
                    message = SlackMessageBuilder.summary(savedTestResults)

                    if (mExtension.showConsoleReports)
                        ConsoleMessageBuilder.summary(savedTestResults)
                }
            } else { // Generic task
                message = SlackMessageBuilder.generic(task, state, mTaskLogBuilder.toString())
            }

            if (message && (mExtension.showCoverage || mExtension.showUnitTest))
                api.call(message)
        }
    }

    boolean shouldMonitorTask(Task task) {
        for (dependentTask in mExtension.dependsOnTasks) {
            if (task.getName() == dependentTask) {
                return true
            }
        }
        return false
    }

    // https://github.com/springfox/springfox/blob/fb780ee1f14627b239fba95730a69900b9b2313a/gradle/coverage.gradle

    private ArrayList<CoverageMetrics> calculateCoverageMetrics(JacocoReport jacoco) {
        if (!jacoco.reports.xml.isEnabled()) {
            throw new Exception("Coverage is unavailable, please enable Jacoco XML coverage reports")
        }

        def file = jacoco.reports.xml.getDestination()
        def parser = new XmlParser()
        parser.setFeature('http://apache.org/xml/features/nonvalidating/load-external-dtd', false)
        parser.setFeature('http://apache.org/xml/features/disallow-doctype-decl', false)
        def xml = parser.parse(file)

        def coverage = new ArrayList<CoverageMetrics>()

        for(def type: mExtension.coverageTypes) {
            def getNumbers = {
                def covered = it.'@covered' as double
                def missed = it.'@missed' as double
                def percentage = ((covered / (covered + missed)) * 100.0).round(2)
                type = type.toLowerCase().capitalize()

                def metrics = new CoverageMetrics()
                metrics.covered = covered
                metrics.missed = missed
                metrics.percentage = percentage
                metrics.type = type

                return metrics
            }

            def getType = xml.counter.find {
                it.'@type' == type.toUpperCase()
            }

            coverage.add(getNumbers(getType))
        }

        // Calculate average

        def percentage = 0.0
        def total = coverage.size().doubleValue()

        for(def metrics : coverage) {
            percentage += metrics.percentage
        }

        def average = new CoverageMetrics()
        average.covered = 0
        average.missed = 0
        average.percentage = ((percentage / total) * 1.0).round(2)
        average.type = 'Average'

        coverage.add(average)

        return coverage
    }
}