plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(23)
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":kookey:annotations"))

    implementation(libs.ksp.api)
    implementation(libs.kotlin.poet)
    implementation(libs.kotlin.poet.ksp)
}
