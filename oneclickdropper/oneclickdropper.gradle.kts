version = "1.0.7"

project.extra["PluginName"] = "One Click Dropper"
project.extra["PluginDescription"] = "Drop items without having to click on them"
project.extra["ProjectSupportUrl"] = "https://github.com/Sundar-Gandu/Pajeet-Plugins"

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
