plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
}

tasks.register<Wrapper>("wrapper") {
    gradleVersion = "9.1.0"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.register("prepareKotlinBuildScriptModel") {
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.femtioprocent.omega.appl.Omega_IS"
//        attributes["Class-Path"] = configurations.compileClasspath.joinToString(separator = " ") { it.name }
    }
}

javafx {
    version = "23"
    modules("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.media", "javafx.graphics")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("de.codecentric.centerdevice:centerdevice-nsmenufx:2.1.4")
    implementation("com.intellij:forms_rt:7.0.3") // probably latest (= 2008)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    implementation("io.arrow-kt:arrow-core:2.1.2")
    implementation("io.arrow-kt:arrow-fx-coroutines:2.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

application {
    mainClass = "com.femtioprocent.omega.appl.Omega_IS"
    executableDir = "../runtime"
}

java {
    toolchain {
	languageVersion.set(JavaLanguageVersion.of(23))
    }
}