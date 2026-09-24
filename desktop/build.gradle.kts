import java.net.URI
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    id("org.jetbrains.compose.hot-reload")
}

kotlin {
    jvmToolchain(17)
}

val generateBuildInfo = tasks.register("generateBuildInfo") {
    description = "Generates BuildInfo.kt with the version from the version catalog"
    group = "build"
    val version = libs.versions.appVersion.get()
    val outDir = layout.buildDirectory.dir("generated/buildinfo")
    inputs.property("version", version)
    outputs.dir(outDir)
    doLast {
        val file = outDir.get().file("com/infinity/drive/desktop/BuildInfo.kt").asFile
        file.parentFile.mkdirs()
        val content = listOf(
            "package com.infinity.drive.desktop",
            "",
            "object BuildInfo {",
            "    const val VERSION = \"$version\"",
            "}",
            ""
        )
        file.writeText(content.joinToString(System.lineSeparator()))
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateBuildInfo)
}

compose.resources {
    packageOfResClass = "com.infinity.drive.desktop.resources"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":ui"))
    implementation(compose.desktop.currentOs)
    implementation(libs.cmp.components.resources)
    implementation(libs.cmp.material3)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.coil.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.jna.platform)
    implementation(libs.vlcj)
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
}

val vlcVersion = "3.0.21"
val vlcZip = layout.buildDirectory.file("vlc/vlc-$vlcVersion-win64.zip")
val isLinuxHost = System.getProperty("os.name").orEmpty().contains("linux", ignoreCase = true)

val downloadVlc = tasks.register("downloadVlc") {
    description = "Downloads the VLC natives archive for bundling"
    group = "build"
    notCompatibleWithConfigurationCache("Downloads an external platform-specific native archive")
    onlyIf { !isLinuxHost }
    val zipFile = vlcZip
    val archiveName = "vlc-$vlcVersion-win64.zip"
    val attempts = 3
    val connectTimeoutMs = 30_000
    val readTimeoutMs = 120_000
    val retryBackoffMs = 5_000L
    val mirrors = listOf(
        "https://download.videolan.org/pub/videolan/vlc/$vlcVersion/win64/$archiveName",
        "https://get.videolan.org/vlc/$vlcVersion/win64/$archiveName"
    )
    outputs.file(zipFile)
    doLast {
        val target = zipFile.get().asFile
        if (target.length() > 0) return@doLast
        target.parentFile.mkdirs()

        var lastFailure: Exception? = null
        repeat(attempts) { attempt ->
            for (mirror in mirrors) {
                try {
                    val connection = URI(mirror).toURL().openConnection().apply {
                        connectTimeout = connectTimeoutMs
                        readTimeout = readTimeoutMs
                    }
                    connection.getInputStream().use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (target.length() > 0) return@doLast
                } catch (e: Exception) {
                    lastFailure = e
                    logger.warn("VLC download from $mirror failed: ${e.message}")
                    target.delete()
                }
            }
            if (attempt < attempts - 1) {
                Thread.sleep((attempt + 1) * retryBackoffMs)
            }
        }
        throw GradleException("Could not download $archiveName from any mirror", lastFailure)
    }
}

val prepareVlcNatives = tasks.register<Copy>("prepareVlcNatives") {
    description = "Unpacks the VLC libraries the inline player loads"
    group = "build"
    notCompatibleWithConfigurationCache("Stages platform-specific native libraries")
    onlyIf { !isLinuxHost }
    dependsOn(downloadVlc)
    from(zipTree(vlcZip)) {
        include("vlc-$vlcVersion/libvlc.dll")
        include("vlc-$vlcVersion/libvlccore.dll")
        include("vlc-$vlcVersion/plugins/**")
        exclude("vlc-$vlcVersion/plugins/gui/**")
        exclude("vlc-$vlcVersion/plugins/lua/**")
        eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
        includeEmptyDirs = false
    }
    into(layout.buildDirectory.dir("appResources/windows-x64/vlc"))
}

val hostArch = System.getProperty("os.arch").orEmpty().lowercase()
val appImageArch = when {
    hostArch == "aarch64" || hostArch == "arm64" -> "aarch64"
    else -> "x86_64"
}

val appImageTool = layout.buildDirectory.file("tools/appimagetool-$appImageArch.AppImage")
val downloadAppImageTool = tasks.register("downloadAppImageTool") {
    description = "Downloads appimagetool for assembling the Linux AppImage"
    group = "distribution"
    notCompatibleWithConfigurationCache("Downloads and prepares an external AppImage tool")
    outputs.file(appImageTool)
    doLast {
        val target = appImageTool.get().asFile
        if (target.length() > 0) return@doLast
        target.parentFile.mkdirs()
        URI("https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-$appImageArch.AppImage")
            .toURL()
            .openStream()
            .use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        check(target.length() > 0) { "Downloaded appimagetool is empty" }
        check(target.setExecutable(true)) { "Could not make appimagetool executable" }
    }
}

val buildAppImage = tasks.register("buildAppImage") {
    description = "Builds a single-file Linux AppImage from the Compose desktop distribution"
    group = "distribution"
    notCompatibleWithConfigurationCache("Runs the external appimagetool process")
    dependsOn("packageAppImage", downloadAppImageTool)
    val appDir = layout.buildDirectory.dir("compose/binaries/main/app/DriveInfinity")
    val output = layout.buildDirectory.file("compose/binaries/main/appimage/DriveInfinity-$appImageArch.AppImage")
    outputs.file(output)
    doLast {
        val directory = appDir.get().asFile
        check(directory.isDirectory) { "Compose Linux application directory is missing: $directory" }
        File(directory, "DriveInfinity.desktop").writeText(
            """[Desktop Entry]
Type=Application
Name=DriveInfinity
Comment=Private cloud storage on your own Telegram channel
Exec=DriveInfinity
Icon=DriveInfinity
Categories=Network;FileTransfer;
Terminal=false
"""
        )
        File(directory, "AppRun").apply {
            writeText("#!/bin/sh\nexec \"\$(dirname \"\$0\")/bin/DriveInfinity\" \"\$@\"\n")
            check(setExecutable(true)) { "Could not make AppRun executable" }
        }
        val icon = File(directory, "lib/DriveInfinity.png")
        check(icon.isFile) { "Linux application icon is missing: $icon" }
        icon.copyTo(File(directory, "DriveInfinity.png"), overwrite = true)
        val target = output.get().asFile
        target.parentFile.mkdirs()
        val tool = appImageTool.get().asFile
        val exitCode = ProcessBuilder(
            tool.absolutePath,
            "--appimage-extract-and-run",
            "--no-appstream",
            directory.absolutePath,
            target.absolutePath
        )
            .inheritIO()
            .start()
            .waitFor()
        check(exitCode == 0) { "appimagetool failed with exit code $exitCode" }
        check(target.isFile && target.length() > 0) { "AppImage was not created: $target" }
        check(target.setExecutable(true)) { "Could not make AppImage executable" }
    }
}

tasks.matching {
    it.name in setOf(
        "run",
        "hotRun",
        "packageMsi",
        "packageDmg",
        "createDistributable",
        "packageDistributionForCurrentOS"
    ) || it.name.startsWith("prepareAppResources")
}.configureEach { dependsOn(prepareVlcNatives) }

compose.desktop {
    application {
        mainClass = "com.infinity.drive.desktop.MainKt"
        providers.gradleProperty("desktopJavaHome").orNull?.let { javaHome = it }

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage, TargetFormat.Dmg)
            appResourcesRootDir.set(layout.buildDirectory.dir("appResources"))
            packageName = "DriveInfinity"
            packageVersion = libs.versions.appVersion.get()
                .split(".")
                .let { parts -> (parts + List(3) { "0" }).take(3) }
                .joinToString(".")
            description = "Private cloud storage on your own Telegram channel"
            modules(
                "java.instrument",
                "java.naming",
                "java.sql",
                "jdk.crypto.ec",
                "jdk.httpserver",
                "jdk.unsupported"
            )
            vendor = "ZHINFINITY"
            licenseFile.set(rootProject.file("LICENSE"))

            windows {
                iconFile.set(project.file("icons/DriveInfinity.ico"))
                perUserInstall = true
                menuGroup = "DriveInfinity"
                shortcut = true
                dirChooser = true
                upgradeUuid = "b7e35f74-33a4-43d9-98b1-84babb95f8a7"
            }

            linux {
                iconFile.set(project.file("icons/DriveInfinity.png"))
            }

            macOS {
                iconFile.set(project.file("icons/DriveInfinity.icns"))
                bundleID = "com.infinity.drive"
            }
        }
    }
}
