package tech.oliver.gradle.slack

class SlackPluginExtension {
    boolean calculateAverageCoverage
    double coverageGood
    ArrayList<String> coverageTypes
    double coverageWarn
    ArrayList<String> dependsOnTasks
    boolean enabled
    boolean showCoverage
    boolean showConsoleReports
    boolean showUnitTest
    String url

    SlackPluginExtension() {
        calculateAverageCoverage = true
        coverageGood = 90.0
        coverageTypes = Arrays.asList("instruction")
        coverageWarn = 80.0
        enabled = true
        showCoverage = true
        showConsoleReports = true
        showUnitTest = true
    }

    void coverageTypes(String... types) {
        this.coverageTypes = Arrays.asList(types)
    }

    void dependsOnTasks(String... tasks) {
        this.dependsOnTasks = Arrays.asList(tasks)
    }
}
