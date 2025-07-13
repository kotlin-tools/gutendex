plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.dokka") version "1.9.10"
    id("jacoco")
    application
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
}

group = "com.github.kupolak.gutendex"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

application {
    mainClass.set("com.github.kupolak.gutendex.ExampleKt")
}

tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))

    dokkaSourceSets {
        named("main") {
            moduleName.set("Gutendex Kotlin Client")
            moduleVersion.set(version.toString())
            includes.from("docs/module.md")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.github.kupolak"
            artifactId = "gutendex"
            version = "1.0.0"
            pom {
                name.set("Gutendex Kotlin Client")
                description.set("Kotlin client for the Gutendex API")
                url.set("https://github.com/kotlin-tools/gutendex")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("kupolak")
                        name.set("kupolak")
                        email.set("jakub.polak.vz@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/kotlin-tools/gutendex.git")
                    developerConnection.set("scm:git:ssh://github.com:kotlin-tools/gutendex.git")
                    url.set("https://github.com/kotlin-tools/gutendex")
                }
            }
        }
    }
}

ktlint {
    version.set("1.1.1")
    android.set(false)
    outputToConsole.set(true)
    filter {
        exclude("**/Example.kt")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("detekt.yml"))
}
