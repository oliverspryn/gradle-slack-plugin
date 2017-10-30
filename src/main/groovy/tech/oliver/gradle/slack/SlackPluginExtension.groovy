package tech.oliver.gradle.slack

class SlackPluginExtension {
    double coverageGood
    double coverageWarn
    List<Object> dependsOnTasks
    boolean enabled
    boolean showConsoleReports
    String url

    SlackPluginExtension() {
        coverageGood = 90.0
        coverageWarn = 80.0
        enabled = true
        showConsoleReports = true
    }

    void dependsOnTasks(Object... paths) {
        this.dependsOnTasks = Arrays.asList(paths)
    }
}
