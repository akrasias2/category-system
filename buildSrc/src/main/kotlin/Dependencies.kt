import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

/** Subprojects from here. */
val DependencyHandlerScope.DOMAIN get() = project(":subproject:domain")/** Subprojects from here. */
val DependencyHandlerScope.INFRASTRUCTURE get() = project(":subproject:infrastructure")
val DependencyHandlerScope.PRESENTATION get() = project(":subproject:presentation")

/** External libraries from here. */
val DependencyHandlerScope.KOTEST get() = "io.kotest:kotest-runner-junit5:${Version.KOTEST}"
val DependencyHandlerScope.KOTEST_ASSERTIONS_ARROW
    get() = "io.kotest.extensions:kotest-assertions-arrow:${Version.KOTEST_ASSERTIONS_ARROW}"

val DependencyHandlerScope.kotest_extensions_spring get() = "io.kotest.extensions:kotest-extensions-spring:1.1.2"

val DependencyHandlerScope.MOCK_K get() = "io.mockk:mockk:${Version.MOCK_K}"

val DependencyHandlerScope.KOTLINX_COROUTINES_CORE
    get() = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.KOTLINX_COROUTINES}"
val DependencyHandlerScope.KOTLINX_COROUTINES_REACTIVE get() = "org.jetbrains.kotlinx:kotlinx-coroutines-reactive"
val DependencyHandlerScope.KOTLINX_COROUTINES_REACTOR get() = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor"

val DependencyHandlerScope.KOTLINX_SERIALIZATION_JSON
    get() = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.KOTLINX_SERIALIZATION_JSON}"

val DependencyHandlerScope.ARROW_CORE get() = "io.arrow-kt:arrow-core:${Version.ARROW}"

val DependencyHandlerScope.LOGBACK get() = "ch.qos.logback:logback-classic:${Version.LOGBACK}"

// Spring boot 추가
val DependencyHandlerScope.spring_boot_dependencies get() = "org.springframework.boot:spring-boot-dependencies:${Version.SPRING_BOOT}"
val DependencyHandlerScope.spring_boot_starter get() = "org.springframework.boot:spring-boot-starter"
val DependencyHandlerScope.spring_boot_starter_web get() = "org.springframework.boot:spring-boot-starter-web"
val DependencyHandlerScope.spring_boot_starter_webflux get() = "org.springframework.boot:spring-boot-starter-webflux"
val DependencyHandlerScope.spring_boot_starter_actuator get() = "org.springframework.boot:spring-boot-starter-actuator"
val DependencyHandlerScope.spring_boot_starter_cache get() = "org.springframework.boot:spring-boot-starter-cache"
val DependencyHandlerScope.spring_boot_configuration_processor get() = "org.springframework.boot:spring-boot-configuration-processor"
val DependencyHandlerScope.spring_boot_starter_test get() = "org.springframework.boot:spring-boot-starter-test"
val DependencyHandlerScope.kotlin_bom get() = "org.jetbrains.kotlin:kotlin-bom:${Version.KOTLIN}"
val DependencyHandlerScope.springmock_k get() = "com.ninja-squad:springmockk:${Version.SPRINGMOCK_K}"
val DependencyHandlerScope.h2_console get() = "me.yaman.can:spring-boot-webflux-h2-console:0.0.1-SNAPSHOT"


val DependencyHandlerScope.TYPESAFE_CONFIG get() = "com.typesafe:config:${Version.TYPESAFE_CONFIG}"

val DependencyHandlerScope.SPRING_DATA_R2DBC get() = "org.springframework.boot:spring-boot-starter-data-r2dbc"
val DependencyHandlerScope.R2DBC_BOM get() = platform("io.r2dbc:r2dbc-bom:${Version.R2DBC_BOM}")
val DependencyHandlerScope.R2DBC_H2 get() = "io.r2dbc:r2dbc-h2"
val DependencyHandlerScope.H2 get() = "com.h2database:h2"
