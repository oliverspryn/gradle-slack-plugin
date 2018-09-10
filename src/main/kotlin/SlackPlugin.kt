import config.SlackPluginConstants
import config.SlackPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class SlackPlugin : Plugin<Project> {

    var extension: SlackPluginExtension? = null

    override fun apply(project: Project?) {
        extension = project?.extensions?.create(SlackPluginConstants.name, SlackPluginExtension::class.java)
    }
}
