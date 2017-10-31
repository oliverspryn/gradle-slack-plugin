package tech.oliver.gradle.slack.models

import org.gradle.api.tasks.testing.TestResult
import tech.oliver.gradle.slack.SlackPluginExtension
import tech.oliver.gradle.slack.utils.TwoColumnTablePrinter

class ConsoleMessageBuilder {
    static coverage(SlackPluginExtension configuration, ArrayList<CoverageMetrics> metrics) {
        TwoColumnTablePrinter table = new TwoColumnTablePrinter("Coverage")

        for(def coverage : metrics) {
            if (!configuration.calculateAverageCoverage && coverage.type.toLowerCase() == "average") continue

            table.addRow(coverage.type, "${coverage.percentage.toString()}%")
        }

        println(table.getFormattedTable())
    }

    static summary(TestResult results) {
        boolean passed = results.failedTestCount == 0
        TwoColumnTablePrinter table = new TwoColumnTablePrinter("Unit Test Summary")

        table.addRow("Successful Tests", results.successfulTestCount.toString())
        table.addRow("Failed Tests", results.failedTestCount.toString())
        table.addRow("Skipped Tests", results.skippedTestCount.toString())
        table.addRow("Total Tests", results.testCount.toString())
        table.addRow("Result", passed ? "Success" : "Failed")

        println(table.getFormattedTable())
    }
}
