plugins {
	java
	checkstyle
}

group = "io.github.skylot.raung"
version = System.getenv("RAUNG_VERSION") ?: "dev"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {

	testImplementation("ch.qos.logback:logback-classic:1.3.5")
	testImplementation("org.assertj:assertj-core:3.24.2")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.test {
	useJUnitPlatform()
}
