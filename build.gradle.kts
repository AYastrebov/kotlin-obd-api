import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jvm)
    `maven-publish`
}

group = "com.github.eltonvs"
version = "1.4.0"

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.io.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}

publishing {
    publications {
        create<MavenPublication>("kotlin-obd-api") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name = "Kotlin OBD API"
                description = "A lightweight and developer-driven API to query and parse OBD commands"
                url = "https://github.com/eltonvs/kotlin-obd-api"

                licenses {
                    license {
                        name = "Apache License v2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }

                developers {
                    developer {
                        id = "eltonvs"
                        name = "Elton Viana"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/eltonvs/kotlin-obd-api.git"
                    developerConnection = "scm:git:git@github.com:eltonvs/kotlin-obd-api.git"
                    url = "https://github.com/eltonvs/kotlin-obd-api"
                }
            }
        }
    }
}
