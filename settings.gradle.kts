rootProject.name = "kookey"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":kookey:annotations")
include(":kookey:ksp-processor")
include(":basic")
