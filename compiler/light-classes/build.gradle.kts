plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":analysis:light-classes-base"))
    api(project(":compiler:util"))
    api(project(":compiler:backend"))
    implementation(project(":compiler:backend.common.jvm"))
    implementation(project(":compiler:frontend"))
    implementation(project(":compiler:frontend.java"))
    implementation(project(":compiler:resolution"))
    implementation(project(":core:descriptors"))
    implementation(project(":core:descriptors.jvm"))
    compileOnly(intellijCore())
    compileOnly(libs.intellij.asm)
    compileOnly(libs.guava)
    compileOnly(libs.intellij.fastutil)
}

sourceSets {
    "main" { projectDefault() }
}
