/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.locateOrRegisterSwiftPMDependenciesExtension
import org.jetbrains.kotlin.gradle.testing.prettyPrinted
import org.jetbrains.kotlin.gradle.util.buildProject
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import kotlin.test.Test
import kotlin.test.assertEquals

class SwiftPMImportExtensionTests {

    @Test
    fun `inferred package names`() {
        val rootProject = buildProject()
        val inferredPackageNames = buildProjectWithMPP(
            projectBuilder = {
                withName("kmp_subproject")
                withParent(rootProject)
            }
        ) {
            locateOrRegisterSwiftPMDependenciesExtension().apply {
                swiftPackage("https://foo.bar/package1", "1.2.3", listOf("product"))
                swiftPackage("https://foo.bar/package2.git", "1.2.3", listOf("product"))
                localSwiftPackage(project.layout.projectDirectory.dir("."), listOf("product"))
                localSwiftPackage(project.layout.projectDirectory.dir("../relativePackage"), listOf("product"))
                localSwiftPackage(project.layout.projectDirectory.dir("package"), listOf("product"))
                localSwiftPackage(project.layout.projectDirectory.dir("sub/subpackage"), listOf("product"))
            }
        }.locateOrRegisterSwiftPMDependenciesExtension().swiftPMDependencies.map {
            it.packageName
        }

        assertEquals(
            listOf(
                "package1",
                "package2",
                "kmp_subproject",
                "relativePackage",
                "package",
                "subpackage",
            ).prettyPrinted,
            inferredPackageNames.prettyPrinted,
        )
    }

    @Test
    fun `inferred remote package names - URL edge cases`() {
        val inferredPackageNames = buildProjectWithMPP().run {
            locateOrRegisterSwiftPMDependenciesExtension().apply {
                // Canonical https URLs with and without the ".git" suffix
                swiftPackage("https://github.com/foo/bar.git", "1.0.0", listOf("product"))
                swiftPackage("https://github.com/foo/bar", "1.0.0", listOf("product"))
                // Trailing slashes must not produce an empty package name
                swiftPackage("https://github.com/foo/bar/", "1.0.0", listOf("product"))
                swiftPackage("https://github.com/foo/bar.git/", "1.0.0", listOf("product"))
                // scp-style Git locations
                swiftPackage("git@github.com:apple/swift-nio.git", "1.0.0", listOf("product"))
                swiftPackage("git@github.com:swift-nio.git", "1.0.0", listOf("product"))
                // ".git" appearing in the middle of the package name is not a suffix
                swiftPackage("https://example.com/my.gitops.git", "1.0.0", listOf("product"))
                swiftPackage("https://example.com/my.gitops", "1.0.0", listOf("product"))
                // ssh URLs
                swiftPackage("ssh://git@github.com/foo/bar.git", "1.0.0", listOf("product"))
            }.swiftPMDependencies.map { it.packageName }
        }

        assertEquals(
            listOf(
                "bar",
                "bar",
                "bar",
                "bar",
                "swift-nio",
                "swift-nio",
                "my.gitops",
                "my.gitops",
                "bar",
            ).prettyPrinted,
            inferredPackageNames.prettyPrinted,
        )
    }

}
