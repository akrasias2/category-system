plugins {
    kotlin("plugin.serialization") version Version.KOTLIN
}

dependencies {

    implementation(DOMAIN)
    implementation(INFRASTRUCTURE)

    implementation(KOTLINX_SERIALIZATION_JSON)

    implementation(spring_boot_starter_actuator)
    implementation(spring_boot_starter_webflux)
    compileOnly(H2)

    implementation(KOTLINX_COROUTINES_REACTOR)
    implementation(TYPESAFE_CONFIG)

    // for test
    testImplementation(DOMAIN)
    testImplementation(INFRASTRUCTURE)

    testImplementation(spring_boot_starter_test)
    testImplementation(spring_boot_starter_webflux)
    testImplementation(SPRING_DATA_R2DBC)
    testImplementation(R2DBC_H2)
    testImplementation(H2)
}
