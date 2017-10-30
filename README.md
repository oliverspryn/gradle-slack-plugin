[![Build Status](https://travis-ci.org/oliverspryn/gradle-slack-plugin.svg?branch=master)](https://travis-ci.org/oliverspryn/gradle-slack-plugin) [![Maintainability](https://api.codeclimate.com/v1/badges/a8ab706cad4edbac4e88/maintainability)](https://codeclimate.com/github/oliverspryn/gradle-slack-plugin/maintainability) [![](https://jitpack.io/v/com.github.oliverspryn/gradle-slack-plugin.svg)](https://jitpack.io/#com.github.oliverspryn/gradle-slack-plugin)

# Gradle Slack plugin

This Gradle plugin can send messages to a Slack channel in accordance with the various steps of your build lifecycle. It may be useful to integrate with a CI server, share unit testing metrics, and to notify everyone when a Gradle build task has failed.

This project builds upon the original [Gradle Slack plugin](https://github.com/Mindera/gradle-slack-plugin), and adds the ability to report JUnit summaries and Jacoco coverage reports to Slack and the build console.

![Build Passing](docs/passing.png)
![Build Failing](docs/failing.png)

## Usage

The plugin is available in [JitPack](https://jitpack.io/). Just add the following to your buildscript dependencies:

```groovy
buildscript {
    dependencies {
        classpath 'com.github.oliverspryn:gradle-slack-plugin:1.1.0'
    }

    repositories {
        maven { url "https://jitpack.io" }
    }
}

task coverage(type: JacocoReport) {
    // Custom Jacoco coverage task
}

task preBuild() {
    // Generic build step
    // Already present in Android Studio
}

task testDebugUnitTest() {
    // JUnit task
    // Already present in Android Studio
}

slack {
    dependsOnTasks 'preBuild', 'coverage', 'testDebugUnitTest'
    url 'your WebHook URL'
}
```

Apply it:

```groovy
apply plugin: 'tech.oliver.gradle.slack'
```

## Configuration

First, you need to setup Slack to receive incoming messages:

1. Go to *your_team*.slack.com/services/new/incoming-webhook

1. Press Add Incoming WebHooks Integration

1. Grab your WebHook URL

Then in your build.gradle file:

```groovy
slack {
    url 'your WebHook URL'
}
```

By default, everytime a build fails a slack message will be sent to the channel you configured. If a build succeeds nothing happens.

There are more optional fields which enable you to further configure the Slack integration:

```groovy
slack {
    coverageGood 90.0
    coverageWarn 80.0
    dependsOnTasks 'testDebug', 'publishApkRelease'
    enabled = isCDMachine()
    showConsoleReports true
    url 'your WebHook URL'
}
```

* `coverageGood`: `90.0` by default. The minimum acceptable amount indicating whether Slack marks the coverage percentage as green.
* `coverageWarn`: `80.0` by default. The minimum warning amount indicating whether Slack marks the coverage percentage as yellow.
* `dependsOnTasks`: Empty by default. Specify a list of tasks which will trigger a message to Slack, regardless of whether it failed or succeeded.
* `enabled`: `true` by default. A boolean to define whether or not the Slack integration is active, useful to avoid sending messages on your local builds.
* `showConsoleReports`: `true` by default. A boolean indicating whether or not the plugin prints the same output to the build console.

## Credits

[Gradle Slack plugin](https://github.com/Mindera/gradle-slack-plugin) by [Mindera](https://github.com/Mindera), the original project which this fork enhances

[Slack WebHook Java API](https://github.com/gpedro/slack-webhook) by [gpedro](https://github.com/gpedro)

## License

gradle-slack-plugin is available under the MIT license. See the LICENSE file for more information.
