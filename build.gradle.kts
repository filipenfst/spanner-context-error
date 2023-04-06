import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
}

extra["springCloudGcpVersion"] = "4.1.1"
extra["springCloudVersion"] = "2022.0.1"
extra["testcontainersVersion"] = "1.17.6"

dependencies {
    implementation("com.google.cloud:spring-cloud-gcp-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")


    implementation("io.micrometer:context-propagation:1.0.2")
//    implementation("com.google.cloud:cloud-spanner-spring-data-r2dbc:1.2.1-SNAPSHOT")
    implementation("com.google.cloud:cloud-spanner-spring-data-r2dbc:1.2.0")
    implementation("org.liquibase:liquibase-core")
    implementation("com.google.cloudspannerecosystem:liquibase-spanner:4.17.1")
    implementation("com.google.cloud:google-cloud-spanner-jdbc")

    implementation("org.postgresql:r2dbc-postgresql:1.0.1.RELEASE")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")

    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.0.2")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:gcloud:1.17.6")
}

dependencyManagement {
    imports {
        mavenBom("com.google.cloud:spring-cloud-gcp-dependencies:${property("springCloudGcpVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
        mavenBom("io.micrometer:micrometer-bom:1.10.4")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
