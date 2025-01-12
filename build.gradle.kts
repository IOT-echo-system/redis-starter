plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

group = "com.robotutor.iot"
version = "0.0.1"

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "com.robotutor"
            artifactId = "redis-starter"
            version = "1.0.17"

            pom {
                name.set("Redis Starter")
                description.set("A reactive redis starter package")
                url.set("https://maven.pkg.github.com/IOT-echo-system/kafka-starter")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/IOT-echo-system/kafka-starter")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}


repositories {
    mavenCentral()
    fun githubMavenRepository(name: String) {
        maven {
            url = uri("https://maven.pkg.github.com/IOT-echo-system/$name")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    githubMavenRepository("robotutor-tech-utils")
    githubMavenRepository("web-client-starter")
    githubMavenRepository("logging-starter")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.robotutor:robotutor-tech-utils:1.0.25")
    implementation("com.robotutor:logging-starter:1.0.3")
    implementation("com.robotutor:web-client-starter:1.0.5")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("bootJar") {
    enabled = false
}
