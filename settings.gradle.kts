pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven("https://maven.aliyun.com/repository/central")
        mavenCentral()
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven ("https://maven.aliyun.com/repository/central")
        mavenCentral()
    }
}
rootProject.name = "AndroLua-Standalone"
include (":app")
include (":androlua-standlone")
