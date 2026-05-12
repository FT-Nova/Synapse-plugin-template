plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// TODO: update group and version to match your manifest
group = "com.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    modularity.inferModulePath.set(true)
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages-Synapse"
        url = uri("https://maven.pkg.github.com/FTMahringer/Synapse")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // The only dependency plugins may import — do not add Spring, JPA, Redis, etc.
    compileOnly("dev.synapse:synapse-plugin-api:1.0.0")

    testImplementation("dev.synapse:synapse-plugin-api:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-core:5.14.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.0")
    testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("dev.synapse:synapse-plugin-api"))
    }
    archiveClassifier.set("")
    manifest {
        attributes(
            "Plugin-Id" to "your-author/your-provider-name",  // TODO: match manifest.yml
            "Plugin-Version" to version,
            "Plugin-Type" to "model-provider"
        )
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
