dependencies {
    implementation(DOMAIN)
    implementation(TYPESAFE_CONFIG)

    implementation(SPRING_DATA_R2DBC)
    implementation(R2DBC_BOM)
    implementation(R2DBC_H2)

    implementation(KOTLINX_COROUTINES_REACTIVE)
    implementation(KOTLINX_COROUTINES_REACTOR)

    implementation(spring_boot_starter_actuator)
    implementation(spring_boot_starter_webflux)

    // for test
    testImplementation(DOMAIN)
    testImplementation(INFRASTRUCTURE)
    testImplementation(spring_boot_starter_test)
    testImplementation(SPRING_DATA_R2DBC)
    testImplementation(R2DBC_BOM)
    testImplementation(R2DBC_H2)
    testImplementation(H2)
}
