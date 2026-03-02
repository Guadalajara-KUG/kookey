plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    application
}

kotlin {
    jvmToolchain(23)
}

dependencies {
    implementation(project(":kookey:annotations"))
    ksp(project(":kookey:ksp-processor"))
}

application {
    mainClass = "com.kookey.basic.MainKt"
}
