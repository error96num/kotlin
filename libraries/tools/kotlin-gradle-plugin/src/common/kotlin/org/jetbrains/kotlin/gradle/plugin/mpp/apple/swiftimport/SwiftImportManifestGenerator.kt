/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport

import org.jetbrains.kotlin.gradle.utils.buildStringBlock
import org.jetbrains.kotlin.gradle.utils.commaSeparatedEntries
import org.jetbrains.kotlin.gradle.utils.emitListItems

/**
 * Generates Package.swift manifest content for Swift Import synthetic packages.
 */
internal object SwiftImportManifestGenerator {
    /** The swift-tools-version used for generated manifests by default. */
    const val DEFAULT_TOOLS_VERSION = "5.9"

    /**
     * The minimal swift-tools-version that supports SwiftPM traits:
     * `.package(..., traits:)` and `Trait` were introduced in PackageDescription 6.1.
     */
    const val TOOLS_VERSION_WITH_TRAITS = "6.1"

    /**
     * Generates the content of a Package.swift manifest file.
     *
     * @param identifier The package and target identifier
     * @param productType The product type string (e.g., ".dynamic" or ".none")
     * @param platforms List of platform strings (e.g., ".iOS(\"15.0\")")
     * @param repoDependencies List of package dependency declarations
     * @param targetDependencies List of target dependency declarations
     * @param toolsVersion The swift-tools-version to emit at the top of the manifest
     * @return The complete Package.swift manifest content
     */
    fun generateManifest(
        identifier: String,
        productType: String,
        platforms: List<String>,
        repoDependencies: List<String>,
        targetDependencies: List<String>,
        binaryTargets: List<String> = emptyList(),
        toolsVersion: String = DEFAULT_TOOLS_VERSION,
    ): String = buildStringBlock(defaultIndent = "  ") {
        line("// swift-tools-version: $toolsVersion")
        line("import PackageDescription")
        block("let package = Package(", ")") {
            commaSeparatedEntries {
                entry { line("name: \"$identifier\"") }
                entry {
                    block("platforms: [", "]") {
                        emitListItems(platforms)
                    }
                }
                entry {
                    block("products: [", "]") {
                        block(".library(", ")") {
                            commaSeparatedEntries {
                                entry { line("name: \"$identifier\"") }
                                entry { line("type: $productType") }
                                entry { line("targets: [\"$identifier\"]") }
                            }
                        }
                    }
                }
                entry {
                    block("dependencies: [", "]") {
                        emitListItems(repoDependencies)
                    }
                }
                entry {
                    block("targets: [", "]") {
                        commaSeparatedEntries {
                            entry {
                                block(".target(", ")") {
                                    commaSeparatedEntries {
                                        entry { line("name: \"$identifier\"") }
                                        entry {
                                            block("dependencies: [", "]") {
                                                emitListItems(targetDependencies)
                                            }
                                        }
                                    }
                                }
                            }
                            binaryTargets.forEach {
                                entry {
                                    line(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
