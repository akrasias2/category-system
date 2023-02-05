import org.gradle.api.plugins.PluginAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version
import org.gradle.plugin.use.PluginDependenciesSpec

fun PluginAware.applyJavaPlugin() {
    apply(plugin = "java")
}

val PluginDependenciesSpec.kotlinJvm get() = kotlin("jvm") version Version.KOTLIN
fun PluginAware.applyKotlinJvmPlugin() {
    apply(plugin = "org.jetbrains.kotlin.jvm")
}

val PluginDependenciesSpec.ktlint get() = id("org.jlleitschuh.gradle.ktlint") version Version.GRADLE_KTLINT
fun PluginAware.applyGradleKtlintPlugin() {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

val PluginDependenciesSpec.shadow get() = id("com.github.johnrengelman.shadow") version Version.GRADLE_SHADOW


val PluginDependenciesSpec.dockerCompose get() = id("com.avast.gradle.docker-compose") version Version.DOCKER_COMPOSE
fun PluginAware.applyDockerComposePlugin() {
    apply(plugin = "com.avast.gradle.docker-compose")
}

val PluginDependenciesSpec.pluginSpring get() = kotlin("plugin.spring") version Version.KOTLIN
fun PluginAware.applyPluginSpring() {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}
val PluginDependenciesSpec.springBoot get() = id("org.springframework.boot") version Version.SPRING_BOOT
fun PluginAware.applySpringBoot() {
    apply(plugin = "org.springframework.boot")
}
val PluginDependenciesSpec.dependencyManagement get() = id("io.spring.dependency-management") version Version.SPRING_DEPENDENCY_MANAGEMENT
fun PluginAware.applyDependencyManagementPlugin() {
    apply(plugin = "io.spring.dependency-management")
}
