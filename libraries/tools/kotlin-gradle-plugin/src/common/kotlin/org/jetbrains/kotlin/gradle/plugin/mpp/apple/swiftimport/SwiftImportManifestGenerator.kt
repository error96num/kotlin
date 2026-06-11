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
    /**
     * Generates the content of a Package.swift manifest file.
     *
     * @param identifier The package and target identifier
     * @param productType The product type string (e.g., ".dynamic" or ".none")
     * @param platforms List of platform strings (e.g., ".iOS(\"15.0\")")
     * @param repoDependencies List of package dependency declarations
     * @param targetDependencies List of target dependency declarations
     * @param headerComments Explanatory comment lines emitted right after the swift-tools-version line.
     *   Each entry is rendered as a `//` comment line; empty entries render as a bare `//` line
     * @return The complete Package.swift manifest content
     */
    fun generateManifest(
        identifier: String,
        productType: String,
        platforms: List<String>,
        repoDependencies: List<String>,
        targetDependencies: List<String>,
        binaryTargets: List<String> = emptyList(),
        headerComments: List<String> = emptyList(),
    ): String = buildStringBlock(defaultIndent = "  ") {
        // The swift-tools-version declaration must remain on the first line of the manifest
        line("// swift-tools-version: 5.9")
        headerComments.forEach { comment ->
            line(if (comment.isEmpty()) "//" else "// $comment")
        }
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
