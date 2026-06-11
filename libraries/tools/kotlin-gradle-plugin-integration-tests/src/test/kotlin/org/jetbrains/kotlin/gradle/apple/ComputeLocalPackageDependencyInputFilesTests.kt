/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.apple

import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.register
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.ComputeLocalPackageDependencyInputFiles
import org.jetbrains.kotlin.gradle.testbase.*
import org.jetbrains.kotlin.gradle.testing.prettyPrinted
import org.jetbrains.kotlin.gradle.util.runProcess
import org.junit.jupiter.api.condition.OS
import java.io.File
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.test.assertEquals

@OsCondition(
    supportedOn = [OS.MAC],
    enabledOnCI = [OS.MAC],
)
@SwiftPMImportGradlePluginTests
class ComputeLocalPackageDependencyInputFilesTests : KGPBaseTest() {

    @GradleTest
    fun `compute local package dependency task - dumps package layout`(version: GradleVersion) {
        project("empty", version) {
            val packageOne = projectPath.resolve("packageOne").also { it.createDirectories() }.toFile()
            val packageTwo = projectPath.resolve("packageTwo").also { it.createDirectories() }.toFile()

            runProcess(listOf("swift", "package", "init", "--type", "library"), packageOne)
            runProcess(listOf("swift", "package", "init", "--type", "library"), packageTwo)

            plugins {
                kotlin("multiplatform").apply(false)
            }
            buildScriptInjection {
                project.tasks.register<ComputeLocalPackageDependencyInputFiles>("computePackage") {
                    localPackages.set(listOf(packageOne, packageTwo))
                    filesToTrackFromLocalPackages.set(project.layout.projectDirectory.file("result"))
                }
            }
            build("computePackage")

            assertEquals(
                listOf(
                    packageOne.resolve("Package.swift"),
                    packageOne.resolve("Sources/packageOne"),
                    packageTwo.resolve("Package.swift"),
                    packageTwo.resolve("Sources/packageTwo"),
                ).prettyPrinted,
                projectPath.resolve("result").readText().lines().map {
                    File(it)
                }.prettyPrinted
            )
        }
    }

    @GradleTest
    fun `compute local package dependency task - dumps transitive local packages`(version: GradleVersion) {
        project("empty", version) {
            val packageOne = projectPath.resolve("packageOne").also { it.createDirectories() }.toFile()
            val packageTwo = projectPath.resolve("packageTwo").also { it.createDirectories() }.toFile()
            val packageThree = projectPath.resolve("packageThree").also { it.createDirectories() }.toFile()

            runProcess(listOf("swift", "package", "init", "--type", "library"), packageOne)
            runProcess(listOf("swift", "package", "init", "--type", "library"), packageTwo)
            runProcess(listOf("swift", "package", "init", "--type", "library"), packageThree)

            // packageOne -> packageTwo -> packageThree; only packageOne is declared directly
            packageOne.declareLocalPackageDependency("packageOne", "packageTwo")
            packageTwo.declareLocalPackageDependency("packageTwo", "packageThree")

            plugins {
                kotlin("multiplatform").apply(false)
            }
            buildScriptInjection {
                project.tasks.register<ComputeLocalPackageDependencyInputFiles>("computePackage") {
                    localPackages.set(listOf(packageOne))
                    filesToTrackFromLocalPackages.set(project.layout.projectDirectory.file("result"))
                }
            }
            build("computePackage")

            // `swift package describe` reports canonical paths for transitive local packages,
            // so compare canonicalized paths on both sides
            assertEquals(
                listOf(
                    packageOne.resolve("Package.swift"),
                    packageOne.resolve("Sources/packageOne"),
                    packageTwo.resolve("Package.swift"),
                    packageTwo.resolve("Sources/packageTwo"),
                    packageThree.resolve("Package.swift"),
                    packageThree.resolve("Sources/packageThree"),
                ).map { it.canonicalFile }.prettyPrinted,
                projectPath.resolve("result").readText().lines().map {
                    File(it).canonicalFile
                }.prettyPrinted
            )
        }
    }

    private fun File.declareLocalPackageDependency(packageName: String, dependencyName: String) {
        resolve("Package.swift").writeText(
            """
            // swift-tools-version: 5.9
            import PackageDescription

            let package = Package(
                name: "$packageName",
                products: [
                    .library(name: "$packageName", targets: ["$packageName"]),
                ],
                dependencies: [
                    .package(path: "../$dependencyName"),
                ],
                targets: [
                    .target(
                        name: "$packageName",
                        dependencies: [
                            .product(name: "$dependencyName", package: "$dependencyName"),
                        ]
                    ),
                ]
            )
            """.trimIndent()
        )
    }

}