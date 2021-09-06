plugins {
    java
    id("maven-publish")
}

group = "org.chiachat"
version = "1.0.5"

repositories {
    mavenCentral()
    maven("https://artifacts.consensys.net/public/maven/maven/")
}

dependencies {
    implementation("tech.pegasys:jblst:0.3.4-1")
    implementation("org.apache.tuweni:tuweni-ssz:2.0.0")
    implementation("org.apache.tuweni:tuweni-bytes:2.0.0")
    implementation("tech.pegasys:jblst")
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.chiachat"
            artifactId = "jbls"
            version = "1.0.5"
            from(components["java"])
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}