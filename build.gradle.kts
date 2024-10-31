plugins {
	java
	jacoco
	`jvm-test-suite`
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.cmpny"
version = "0.0.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

jacoco {
	toolVersion = "0.8.12"
	reportsDirectory = layout.buildDirectory.dir("customJacocoReportDir")
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("commons-validator:commons-validator:1.9.0")
	implementation("org.apache.commons:commons-lang3:3.17.0")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sourceSets {
	create("integ") {
		java {
			srcDir("src/integ/java")
		}
		resources {
			srcDir("src/main/resources")
		}
		compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath + sourceSets.test.get().compileClasspath
		runtimeClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath + sourceSets.test.get().runtimeClasspath
	}
}

val integrationTest = task<Test>("integ") {
	testClassesDirs = sourceSets["integ"].output.classesDirs
	classpath = sourceSets["integ"].runtimeClasspath
	shouldRunAfter("test")

	useJUnitPlatform()

}

tasks.check { dependsOn(integrationTest) }

tasks.test {
	filter {
		includeTestsMatching("*Test")
		excludeTestsMatching("*IntegrationTest")
	}
}


tasks.jacocoTestReport {
	reports {
		xml.required = false
		csv.required = false
		html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
	}
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}


