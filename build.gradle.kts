import com.github.gradle.node.npm.task.NpmTask

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.node-gradle.node") version "7.0.2"
}

group = "org.octopusden.octopus.dms"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val npmBuild = tasks.register<NpmTask>("npmBuild") {
    dependsOn("npmInstall")
    npmCommand.set(listOf("run", "build"))
}

tasks.withType<ProcessResources> {
    dependsOn(npmBuild)
}

node {
    version.set("16.20.2")
    download.set(true)
    npmVersion.set("8.19.4")
}

tasks.getByName<Delete>("clean") {
    this.delete.add("$projectDir/node_modules")
}

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${project.property("spring-cloud.version")}"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    implementation(platform("org.springframework.boot:spring-boot-dependencies:${project.properties["spring-boot.version"]}"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.octopusden.octopus-cloud-commons:octopus-security-common:${project.properties["cloud-commons.version"]}") {
        exclude("org.springframework.security")
        exclude("org.springframework.boot")
        exclude("org.jetbrains.kotlin")
        exclude("org.springframework.cloud")
    }

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
