plugins {
    kotlin("jvm")
    id("java-test-fixtures")
    id("project-tests-convention")
    id("gradle-plugin-compiler-dependency-configuration")
}

dependencies {
    testFixturesImplementation(libs.opentest4j)
    testFixturesApi(testFixtures(project(":compiler:test-infrastructure-utils.common")))
    testFixturesImplementation(project(":compiler:fir:entrypoint"))
    testFixturesImplementation(project(":compiler:cli"))
    testFixturesImplementation(project(":compiler:cli-jvm"))
    testFixturesImplementation(intellijCore())
    testFixturesImplementation(commonDependency("org.jetbrains.kotlin:kotlin-reflect")) { isTransitive = false }
    testImplementation(kotlin("test"))
    testImplementation(project(":core:util.runtime"))
}

sourceSets {
    "main" { none() }
    "test" { projectDefault() }
    "testFixtures" { projectDefault() }
}

optInToK1Deprecation()

testsJar()

projectTests {
    testTask(jUnitMode = JUnitMode.JUnit5)
}
