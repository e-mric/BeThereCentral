plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.betherecentral.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.betherecentral"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    sourceSets.getByName("main").assets.srcDir("../exploration/dist")
}

dependencies {
    implementation(project(":composeApp"))
    implementation("androidx.activity:activity-compose:1.10.1")
}

val verifyExploreBundle by tasks.registering {
    doLast {
        val requiredFiles = listOf("index.html", "viewer.js", "scene-data.js", "style.css", "SCENE-LICENSE.txt", "PLAYCANVAS-LICENSE.txt")
        val missing = requiredFiles.filterNot { file("../exploration/dist/$it").isFile }
        check(missing.isEmpty()) {
            "Missing offline Explore files: ${missing.joinToString()}. Build exploration/dist before packaging the Android app."
        }
    }
}

tasks.named("preBuild") { dependsOn(verifyExploreBundle) }
