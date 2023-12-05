pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Project3-dtammina"
include(":app")


//include(":sceneform")

//project(":sceneform").projectDir = File("sceneformsrc/sceneform")

//include(":sceneformux")

//project(":sceneformux").projectDir = File("sceneformux/ux")
 