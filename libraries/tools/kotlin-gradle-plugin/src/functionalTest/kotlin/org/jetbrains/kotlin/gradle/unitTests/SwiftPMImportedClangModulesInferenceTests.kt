/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.unitTests

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.SwiftPMDependency
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.SwiftPMImportExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.locateOrRegisterSwiftPMDependenciesExtension
import org.jetbrains.kotlin.gradle.testing.prettyPrinted
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * KT-84698: tests for the `importedClangModules` ("imported modules") inference logic.
 *
 * When module discovery is disabled, the cinterop Clang modules are taken from the explicit
 * `importedClangModules` declarations. By default these are inferred from the declared products:
 * - products without platform constraints and without per-product module overrides contribute a module
 *   matching the product name without platform constraints,
 * - platform-constrained products contribute a module matching the product name constrained to the same platforms,
 * - per-product `importedClangModules` overrides replace the product-name guess for that product.
 */
class SwiftPMImportedClangModulesInferenceTests {

    private fun inferredClangModules(
        configure: SwiftPMImportExtension.(project: Project) -> Unit,
    ): List<Pair<String, Set<SwiftPMDependency.Platform>?>> {
        val project = buildProjectWithMPP()
        return project.locateOrRegisterSwiftPMDependenciesExtension()
            .apply { configure(project) }
            .swiftPMDependencies.single()
            .cinteropClangModules.map { it.name to it.platformConstraints }
    }

    @Test
    fun `remote package with string products - imported modules default to product names`() {
        assertEquals(
            listOf<Pair<String, Set<SwiftPMDependency.Platform>?>>(
                "ProductA" to null,
                "ProductB" to null,
            ).prettyPrinted,
            inferredClangModules {
                swiftPackage(
                    url = "https://github.com/foo/bar.git",
                    version = "1.0.0",
                    products = listOf("ProductA", "ProductB"),
                )
            }.prettyPrinted,
        )
    }

    @Test
    fun `remote package with string products - explicit importedClangModules replace the inferred modules`() {
        assertEquals(
            listOf<Pair<String, Set<SwiftPMDependency.Platform>?>>(
                "ClangModuleA" to null,
                "ClangModuleB" to null,
            ).prettyPrinted,
            inferredClangModules {
                swiftPackage(
                    url = "https://github.com/foo/bar.git",
                    version = "1.0.0",
                    products = listOf("ProductA"),
                    importedClangModules = listOf("ClangModuleA", "ClangModuleB"),
                )
            }.prettyPrinted,
        )
    }

    @Test
    fun `remote package with typed products - inference accounts for platform constraints and per-product overrides`() {
        assertEquals(
            listOf(
                // Unconstrained products without overrides are inferred at the package level
                "PlainProduct" to null,
                // Platform-constrained products contribute a module named after the product with the same constraints
                "IOSOnlyProduct" to setOf(SwiftPMDependency.Platform.iOS),
                // Per-product overrides replace the product-name guess
                "CustomClangModule" to null,
            ).prettyPrinted,
            inferredClangModules {
                swiftPackage(
                    url = url("https://github.com/foo/bar.git"),
                    version = from("1.0.0"),
                    products = listOf(
                        product("PlainProduct"),
                        product("IOSOnlyProduct", platforms = setOf(iOS())),
                        product("CustomProduct", importedClangModules = setOf("CustomClangModule")),
                    ),
                )
            }.prettyPrinted,
        )
    }

    @Test
    fun `remote package with typed products - platform-constrained product with explicit modules`() {
        assertEquals(
            listOf<Pair<String, Set<SwiftPMDependency.Platform>?>>(
                "BarCore" to setOf(SwiftPMDependency.Platform.iOS, SwiftPMDependency.Platform.macOS),
                "BarUI" to setOf(SwiftPMDependency.Platform.iOS, SwiftPMDependency.Platform.macOS),
            ).prettyPrinted,
            inferredClangModules {
                swiftPackage(
                    url = url("https://github.com/foo/bar.git"),
                    version = from("1.0.0"),
                    products = listOf(
                        product(
                            "BarProduct",
                            platforms = setOf(iOS(), macOS()),
                            importedClangModules = setOf("BarCore", "BarUI"),
                        ),
                    ),
                )
            }.prettyPrinted,
        )
    }

    @Test
    fun `local package with string products - imported modules default to product names`() {
        assertEquals(
            listOf<Pair<String, Set<SwiftPMDependency.Platform>?>>(
                "LocalProduct" to null,
            ).prettyPrinted,
            inferredClangModules { project ->
                localSwiftPackage(
                    directory = project.layout.projectDirectory.dir("LocalPackage"),
                    products = listOf("LocalProduct"),
                )
            }.prettyPrinted,
        )
    }

    @Test
    fun `local package with typed products - inference accounts for platform constraints and per-product overrides`() {
        assertEquals(
            listOf(
                "PlainProduct" to null,
                "WatchOSOnlyProduct" to setOf(SwiftPMDependency.Platform.watchOS),
                "CustomClangModule" to null,
            ).prettyPrinted,
            inferredClangModules { project ->
                localSwiftPackage(
                    directory = project.layout.projectDirectory.dir("LocalPackage"),
                    products = listOf(
                        product("PlainProduct"),
                        product("WatchOSOnlyProduct", platforms = setOf(watchOS())),
                        product("CustomProduct", importedClangModules = setOf("CustomClangModule")),
                    ),
                )
            }.prettyPrinted,
        )
    }
}
