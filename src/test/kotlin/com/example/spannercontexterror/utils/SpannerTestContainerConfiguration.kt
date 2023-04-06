package com.example.spannercontexterror.utils

import com.example.spannercontexterror.IntegrationTest
import com.google.cloud.NoCredentials
import com.google.cloud.spanner.DatabaseId
import com.google.cloud.spanner.InstanceConfigId
import com.google.cloud.spanner.InstanceId
import com.google.cloud.spanner.InstanceInfo
import com.google.cloud.spanner.Spanner
import com.google.cloud.spanner.SpannerOptions
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.SpannerEmulatorContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.ExecutionException

private const val PROJECT_NAME = "test-project"
private const val INSTANCE_NAME = "test-instance"
private const val DATABASE_NAME = "test-db"

internal object SpannerTestContainerConfiguration : ApplicationContextInitializer<ConfigurableApplicationContext> {

    private val emulator: SpannerEmulatorContainer = SpannerEmulatorContainer(
        DockerImageName.parse("gcr.io/cloud-spanner-emulator/emulator:1.4.9"),
    )

    fun spannerSetup() {
        emulator.start()
        val spanner = SpannerOptions.newBuilder()
            .setEmulatorHost(emulator.emulatorGrpcEndpoint)
            .setCredentials(NoCredentials.getInstance())
            .setProjectId(PROJECT_NAME)
            .build()
            .service

        createInstance(spanner)
        val databaseId = createDatabase(spanner)
        spanner.getDatabaseClient(databaseId)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    private fun createDatabase(spanner: Spanner): DatabaseId {
        val statements = listOf("CREATE TABLE context_test_table (id STRING(MAX), value STRING(MAX)) PRIMARY KEY (id)")
        return spanner.databaseAdminClient
            .createDatabase(INSTANCE_NAME, DATABASE_NAME, statements)
            .get()
            .id
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    private fun createInstance(spanner: Spanner): InstanceId {
        val instanceId = InstanceId.of(PROJECT_NAME, INSTANCE_NAME)
        val instanceConfig = InstanceConfigId.of(PROJECT_NAME, "emulator-config")
        spanner.instanceAdminClient
            .createInstance(
                InstanceInfo.newBuilder(instanceId)
                    .setNodeCount(1)
                    .setDisplayName("Test instance")
                    .setInstanceConfigId(instanceConfig)
                    .build(),
            )
            .get()
        return instanceId
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        spannerSetup()
        IntegrationTest.environmentVariables.set("SPANNER_EMULATOR_HOST", emulator.emulatorGrpcEndpoint)
        val cloudSpannerUrl = "cloudspanner://${emulator.emulatorGrpcEndpoint}/" +
                "projects/$PROJECT_NAME/" +
                "instances/$INSTANCE_NAME/" +
                "databases/$DATABASE_NAME?" +
                "autoConfigEmulator=true"
        TestPropertyValues.of(
            mapOf(
                "spring.r2dbc.url" to "r2dbc:$cloudSpannerUrl",
                "spring.datasource.url" to "jdbc:$cloudSpannerUrl",
                "spring.cloud.gcp.project-idt" to PROJECT_NAME,
                "spring.cloud.gcp.spanner.emulator.enabled" to "true",
                "spring.cloud.gcp.spanner.database" to DATABASE_NAME,
                "spring.cloud.gcp.spanner.instance-id" to INSTANCE_NAME,
                "spring.cloud.gcp.spanner.project-id" to PROJECT_NAME
            )
        ).applyTo(applicationContext)
    }
}