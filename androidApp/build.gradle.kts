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
    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/communityStudioAssets").get().asFile.absolutePath)
    androidResources {
        // Keep SvelteKit's _app directory; aapt's default underscore filter drops it.
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }
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

val buildCommunityStudio by tasks.registering(Exec::class) {
    workingDir(rootProject.file("community-studio"))
    commandLine("npm", "run", "build")
    inputs.files(fileTree("../community-studio/src"), fileTree("../content/community"))
    inputs.files(fileTree("../community-studio/scripts"))
    inputs.files(fileTree("../community-studio/static") { exclude("assets/community/**") })
    inputs.files("../community-studio/package.json", "../community-studio/package-lock.json", "../community-studio/vite.config.ts", "../community-studio/tsconfig.json")
    outputs.dir("../community-studio/build")
}

val stageCommunityStudio by tasks.registering(Sync::class) {
    dependsOn(buildCommunityStudio)
    from(rootProject.file("community-studio/build")) { into("community-studio") }
    into(layout.buildDirectory.dir("generated/communityStudioAssets"))
    doLast {
        check(layout.buildDirectory.file("generated/communityStudioAssets/community-studio/200.html").get().asFile.isFile) {
            "Community Studio's offline entry is missing. Install the browser dependencies with npm ci in community-studio."
        }
    }
}

tasks.named("preBuild") { dependsOn(stageCommunityStudio) }
