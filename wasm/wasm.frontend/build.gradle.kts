plugins {
    kotlin("jvm")
    id("generated-sources")
}

dependencies {
    api(project(":compiler:util"))
    api(project(":compiler:frontend"))
    api(project(":js:js.frontend"))
    api(project(":wasm:wasm.config"))
    compileOnly(intellijCore())
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

optInToK1Deprecation()

generatedConfigurationKeys("WasmConfigurationKeys")
