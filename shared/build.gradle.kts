plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.infinity.drive.shared"
        compileSdk = 37
        minSdk = 26
    }

    jvm("desktop")

    val osName = System.getProperty("os.name").orEmpty().lowercase()
    val osArch = System.getProperty("os.arch").orEmpty().lowercase()
    val isArm64 = osArch == "aarch64" || osArch == "arm64"
    val desktopNativeClassifier = when {
        osName.contains("win") ->
            "windows_amd64"
        osName.contains("linux") && isArm64 ->
            "linux_arm64_gnu_ssl3"
        osName.contains("linux") ->
            "linux_amd64_gnu_ssl3"
        osName.contains("mac") ->
            // Apple Silicon is arm64; Intel Macs are rare for new builds
            if (isArm64) "macos_arm64" else "macos_amd64"
        else -> error("Unsupported desktop operating system for TDLight natives: os=$osName arch=$osArch")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                api(libs.androidx.paging.common)
                api(libs.androidx.room.runtime)
                api(libs.androidx.room.paging)
                api(libs.androidx.sqlite.bundled)
                api(libs.androidx.datastore.preferences.core)
            }
        }
        val jvmCommonMain = create("jvmCommonMain") {
            dependsOn(commonMain.get())
        }
        val jvmCommonTest = create("jvmCommonTest") {
            dependsOn(commonTest.get())
        }
        named("androidMain") {
            dependsOn(jvmCommonMain)
        }
        named("desktopMain") {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation(libs.tdlight.java)
                implementation(
                    "it.tdlight:tdlight-natives:${libs.versions.tdlightNatives.get()}:$desktopNativeClassifier"
                )
            }
        }
        named("desktopTest") {
            dependsOn(jvmCommonTest)
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}
