package tech.oliver.gradle.slack.models

import org.gradle.api.tasks.testing.TestResult
import tech.oliver.gradle.slack.utils.TwoColumnTablePrinter

class ConsoleMessageBuilder {

    static coverage(double percentage) {
        TwoColumnTablePrinter table = new TwoColumnTablePrinter("Unit Test")
        table.addRow("Coverage", "${percentage.toString()}%")

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
