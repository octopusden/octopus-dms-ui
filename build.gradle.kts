import com.github.gradle.node.npm.task.NpmTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Duration

plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("com.github.node-gradle.node")
    id("io.github.gradle-nexus.publish-plugin")
    id("com.bmuschko.docker-spring-boot-application")
    idea
    // Kept although nothing is published: the release workflow may invoke the `publish`
    // lifecycle task, which then exists as a no-op, and the publication guard below reads
    // `plugins.hasPlugin("maven-publish")` to decide what to inspect.
    `maven-publish`
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.octopusden.octopus-quality")
}

octopusQuality {
    // This repository must publish NOTHING to Maven Central — #19 removed its publication
    // (-47.9 MB and -60 files per release) and `release.yaml` sets `publish-to-nexus: false`.
    // The declared set is therefore deliberately EMPTY, which is a statement, not an omission:
    // the shared policy fails if any publication reappears.
    //
    // The local task that did this until now is deleted in the same commit as the version bump,
    // because octopus-base v2.7.0 registers a task of exactly that name and two tasks of one
    // name fail configuration.
    //
    // Note how this one is verified: with an empty set, GREEN prints NOTHING, so a passing run
    // is not by itself evidence the task ran. RED can only be shown by temporarily ADDING a
    // publication — there is nothing in the declaration to perturb.
    publication {
        enforceCentralPublications.set(true)
        centralPublications.set(emptySet())
    }
    // Repo has no tests / no coverage tool yet — disable coverage verification.
    coverage {
        enabled.set(false)
    }
    // Enforce Kotlin static analysis (detekt + ktlint); current debt is absorbed by
    // detekt-baseline.xml / ktlint-baseline.xml so the gate stays green while enforcing.
    kotlin {
        failOnViolation.set(true)
    }
}

group = "org.octopusden.octopus.dms"

java {
    withJavadocJar()
    withSourcesJar()
    JavaVersion.VERSION_21.let {
        sourceCompatibility = it
        targetCompatibility = it
    }
}

kotlin {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
}

idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.gradle.org/gradle/libs-releases")
    }
}

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${project.property("spring-cloud.version")}"))
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    implementation(platform("org.springframework.boot:spring-boot-dependencies:${project.properties["spring-boot.version"]}"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation(kotlin("test"))
}

ext {
    System.getenv().let {
        set(
            "dockerRegistry",
            System.getenv().getOrDefault("DOCKER_REGISTRY", project.properties["docker.registry"]),
        )
        set(
            "octopusGithubDockerRegistry",
            System
                .getenv()
                .getOrDefault("OCTOPUS_GITHUB_DOCKER_REGISTRY", project.properties["octopus.github.docker.registry"]),
        )
    }
    val mandatoryProperties = mutableListOf("dockerRegistry", "octopusGithubDockerRegistry")
    val undefinedProperties = mandatoryProperties.filter { (project.ext[it] as String).isBlank() }
    if (undefinedProperties.isNotEmpty()) {
        throw IllegalArgumentException(
            "Start gradle build with" +
                (if (undefinedProperties.contains("dockerRegistry")) " -Pdocker.registry=..." else "") +
                (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " -Poctopus.github.docker.registry=..." else "") +
                " or set env variable(s):" +
                (if (undefinedProperties.contains("dockerRegistry")) " DOCKER_REGISTRY" else "") +
                (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " OCTOPUS_GITHUB_DOCKER_REGISTRY" else ""),
        )
    }
}

fun String.getExt() = project.ext[this] as String

springBoot {
    buildInfo()
}

node {
    nodeProjectDir.set(project.rootDir.resolve("frontend"))
    version.set("16.20.2")
    download.set(true)
    npmVersion.set("8.19.4")
}

val npmBuild = tasks.register<NpmTask>("npmBuild") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "build"))
}

tasks.withType<ProcessResources> {
    dependsOn(npmBuild)
}

tasks.getByName<Delete>("clean") {
    this.delete.add("$projectDir/node_modules")
}

tasks.test {
    useJUnitPlatform()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
    transitionCheckOptions {
        maxRetries.set(60)
        delayBetween.set(Duration.ofSeconds(30))
    }
}

// This repository publishes nothing to Maven Central: nothing consumes it as a Maven
// dependency, and its deliverable is the docker image built below. The MavenPublication for
// the bootJar (46 MB) and the `signing` block that signed it are therefore gone. The
// release workflow also passes publish-to-nexus: false — the flag alone would only guard
// the pipeline, while a manual `./gradlew publishToSonatype` with credentials would still
// upload. Both halves, or it is not done.
//
// nexusPublishing above is intentionally left in place: it declares only the destination,
// creates no publication, and keeps publishToSonatype existing as a harmless no-op.

docker {
    springBootApplication {
        baseImage.set("${"dockerRegistry".getExt()}/eclipse-temurin:21-jdk")
        ports.set(listOf(8080))
        images.set(setOf("${"octopusGithubDockerRegistry".getExt()}/octopusden/${project.name}:${project.version}"))
    }
}
