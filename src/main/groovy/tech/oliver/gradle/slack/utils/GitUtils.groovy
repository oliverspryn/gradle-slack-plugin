package tech.oliver.gradle.slack.utils

class GitUtils {

    public static String branchName() {
        def workingBranch = "git rev-parse --abbrev-ref HEAD".execute().text.trim()
        return workingBranch
    }

    public static String lastCommitAuthor() {
        def lastCommitAuthor = "git log -1 --pretty=%ce".execute().text.trim()
        return lastCommitAuthor
    }

    public static String lastCommitMessage() {
        def message = "git log -1 --pretty=%B".execute().text.trim()
        return message
    }
}
