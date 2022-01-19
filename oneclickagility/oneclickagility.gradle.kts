version = "1.1.15"

project.extra["PluginName"] = "One Click Agility"
project.extra["PluginDescription"] = "Reclined gaming"
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
