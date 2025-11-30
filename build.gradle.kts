import nu.studer.gradle.jooq.JooqGenerate
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.Target
import org.springframework.boot.gradle.tasks.bundling.BootJar
import kotlin.apply

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    id("nu.studer.jooq") version "10.1"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "com.vpr42"
version = "1.0.0"

val detektVersion = "1.23.8"
val jooqVersion = "3.19.2"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JOOQ
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.jooq:jooq:$jooqVersion")
    jooqGenerator("org.postgresql:postgresql")
    jooqGenerator("org.jooq:jooq-codegen:$jooqVersion")
    jooqGenerator("org.jooq:jooq-meta:$jooqVersion")
    jooqGenerator("org.jooq:jooq-meta-extensions:$jooqVersion")
    runtimeOnly("org.postgresql:postgresql")

    // Detekt
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:$detektVersion")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    implementation("org.apache.commons:commons-lang3:3.18.0")

    // Logging
    runtimeOnly("io.github.oshai:kotlin-logging-jvm:7.0.7")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(file("$rootDir/detekt.yml"))
    autoCorrect = true
}

jooq {
    version.set(jooqVersion)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"

                    database = Database().apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties = listOf(
                            Property().withKey("scripts")
                                .withValue("src/main/resources/db"),
                            Property().withKey("dialectName")
                                .withValue("POSTGRES"),
                            Property().withKey("defaultNameCase")
                                .withValue("AS_IS")
                        )
                    }

                    generate = Generate().apply {
                        isPojos = true
                        isRecords = true
                        isDaos = false
                        isJpaAnnotations = false

                        isJavaTimeTypes = true

                        // Генерировать Kotlin data class POJO
                        isPojosAsKotlinDataClasses = true
                        isImmutablePojos = true

                        // Сделать non-nullable свойства там, где столбец NOT NULL
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullInterfaceAttributes = true

                        // Отключить defaulted nullable (иначе все поля остаются nullable)
                        isKotlinDefaultedNullablePojoAttributes = false
                    }

                    target = Target().apply {
                        packageName = "com.vpr42.marketplace.jooq"
                        directory = "build/generated/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.named<JooqGenerate>("generateJooq") {
    allInputsDeclared.set(true)
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.named("generateJooq"))
}

tasks.withType<Jar> {
    enabled = false
}

tasks.withType<BootJar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
