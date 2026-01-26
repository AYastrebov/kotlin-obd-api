@file:OptIn(ExperimentalWasmDsl::class)

import com.android.build.api.dsl.androidLibrary
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
}

val deployVersion = findProperty("KObdApiDeployVersion") as String?
version = deployVersion?.removePrefix("v") ?: "1.4.0-SNAPSHOT"
group = "com.github.eltonvs.obd"
description = "Kotlin Multiplatform OBD-II API library"

kotlin {
    explicitApi()
    jvmToolchain(17)

    androidLibrary {
        namespace = "com.github.eltonvs.obd"
        compileSdk = 36
    }
    jvm()
    wasmJs { nodejs() }
    js { nodejs() }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io.core)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.io.bytestring)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
    )

    pom {
        name.set("kotlin-obd-api")
        description.set(project.description)
        url.set("https://github.com/eltonvs/kotlin-obd-api")
        inceptionYear.set("2018")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://opensource.org/licenses/apache-2-0")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("eltonvs")
                name.set("Elton Viana")
                url.set("https://github.com/eltonvs")
            }
        }

        scm {
            url.set("https://github.com/eltonvs/kotlin-obd-api")
            connection.set("scm:git:git://github.com/eltonvs/kotlin-obd-api.git")
            developerConnection.set("scm:git:ssh://github.com/eltonvs/kotlin-obd-api.git")
        }
    }
}

// Configure GitHub Packages repository
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/eltonvs/kotlin-obd-api")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: findProperty("gpr.token") as String?
            }
        }
    }
}

// Dokka documentation configuration
dokka {
    moduleName.set("kotlin-obd-api")
    moduleVersion.set(project.version.toString())

    dokkaSourceSets.configureEach {
        includes.from("Module.md")

        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/eltonvs/kotlin-obd-api/tree/master/kotlin-obd-api/src")
            remoteLineSuffix.set("#L")
        }

        externalDocumentationLinks {
            create("kotlinx.coroutines") {
                url("https://kotlinlang.org/api/kotlinx.coroutines/")
            }
            create("kotlinx.io") {
                url("https://kotlinlang.org/api/kotlinx-io/")
            }
        }
    }

    pluginsConfiguration {
        html {
            footerMessage.set("Copyright &copy; 2018-2025 Elton Viana")
        }
    }
}

tasks.withType<DokkaGenerateTask>().configureEach {
    notCompatibleWithConfigurationCache("Dokka does not support configuration cache")
}