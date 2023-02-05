//import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    java
    kotlinJvm
    application

    ktlint
    shadow

    dockerCompose

    springBoot
    dependencyManagement apply false
    pluginSpring apply false
}

/** Settings for all projects from here. */
allprojects {
    group = "com.assignment"
    version = Version.KOTLIN_SERVER_TEMPLATE

    applyJavaPlugin()
    applyKotlinJvmPlugin()
    applyGradleKtlintPlugin()
    applyDockerComposePlugin()

    applyPluginSpring()
    applySpringBoot()
    applyDependencyManagementPlugin()

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dockerCompose {
        useComposeFiles.set(listOf("${rootProject.rootDir}/docker-compose.yml"))
        setProjectName("kotlin-server-test")
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        // spring boot
        implementation(platform(spring_boot_dependencies))
        implementation(spring_boot_starter)
        annotationProcessor(spring_boot_configuration_processor)
        implementation(platform(kotlin_bom))
        implementation(KOTLINX_COROUTINES_CORE)

        // either
        implementation(ARROW_CORE)

        // test
        testImplementation(KOTEST)
        testImplementation(KOTEST_ASSERTIONS_ARROW)

        testImplementation(MOCK_K)
    }

    /** Lint settings. */
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        disabledRules.set(setOf("no-wildcard-imports"))
    }

    /** Test settings. */
    tasks.withType<Test>().configureEach {
        useJUnitPlatform() // Platform setting for Kotest.
        testLogging.showStandardStreams = true // Prints log while test.
    }

}

/** Settings for only the root project from here. */
dependencies {
    implementation(PRESENTATION)
}

springBoot {
    mainClass.set("com.assignment.ktserver.AssignmentApplicationKt")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "com.assignment.ktserver.AssignmentApplicationKt"))
        }
    }

    /** Builds a Docker image with the current project, and then publish it to local Docker registry. */
    register("publishDocker") {
        dependsOn("installDist")
        doLast {
            exec {
                commandLine(
                    "docker",
                    "build",
                    "-t",
                    "kotlin-server-template:latest",
                    "-t",
                    "kotlin-server-template:$version",
                    "."
                )
            }
        }
    }
}
