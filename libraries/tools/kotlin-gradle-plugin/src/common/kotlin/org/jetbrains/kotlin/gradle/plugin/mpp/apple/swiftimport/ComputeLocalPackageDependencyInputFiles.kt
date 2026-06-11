/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.utils.getFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault(because = "KT-84827 - SwiftPM import doesn't support caching yet")
internal abstract class ComputeLocalPackageDependencyInputFiles : DefaultTask() {

    @get:Input
    val localPackages: SetProperty<File> = project.objects.setProperty(File::class.java)

    /**
     * Recompute if the manifests change
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    protected val manifests get() = localPackages.map { it.map { it.resolve("Package.swift") } }

    @get:OutputFile
    val filesToTrackFromLocalPackages: RegularFileProperty = project.objects.fileProperty().convention(
        project.layout.buildDirectory.file("kotlin/swiftImportFilesToTrackFromLocalPackages")
    )

    /**
     * KT-84800: Recompute if the manifests of transitive local packages discovered by the previous
     * run change. The direct package manifests are covered by [manifests]; the transitive ones are
     * only known after execution, so we track the manifests recorded in the previous output. A
     * change to a direct or already discovered manifest re-triggers discovery, which picks up any
     * newly added transitive local packages.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    protected val previouslyDiscoveredManifests: Provider<List<File>>
        get() = filesToTrackFromLocalPackages.map { output ->
            val outputFile = output.asFile
            if (outputFile.exists()) {
                outputFile.readLines()
                    .filter { it.isNotEmpty() && it.endsWith(MANIFEST_FILE_NAME) }
                    .map(::File)
            } else {
                emptyList()
            }
        }

    @get:Inject
    protected abstract val execOps: ExecOperations

    @TaskAction
    fun generateSwiftPMSyntheticImportProjectAndFetchPackages() {
        // KT-84800: direct local packages may transitively depend on other local packages; walk
        // the local dependency graph so that the sources of transitive local packages are
        // fingerprinted as well.
        val visitedPackageRoots = hashSetOf<String>()
        val packageRootsQueue = ArrayDeque(localPackages.get())
        val localPackageFiles = mutableListOf<File>()
        while (packageRootsQueue.isNotEmpty()) {
            val packageRoot = packageRootsQueue.removeFirst()
            if (!visitedPackageRoots.add(packageRoot.canonicalPath)) continue
            val packageDescription = describeLocalPackage(packageRoot)
            localPackageFiles.add(packageRoot.resolve(MANIFEST_FILE_NAME))
            localPackageFiles.addAll(findLocalPackageSources(packageRoot, packageDescription))
            packageRootsQueue.addAll(findTransitiveLocalPackages(packageDescription))
        }
        filesToTrackFromLocalPackages.getFile().writeText(
            localPackageFiles.joinToString("\n") { it.path }
        )
    }

    @Serializable
    data class PackageDescription(
        val targets: List<PackageTarget>,
        val dependencies: List<PackageDependency> = emptyList(),
    ) {
        @Serializable
        data class PackageTarget(
            val path: String,
            val type: String,
            @kotlinx.serialization.SerialName("module_type") val moduleType: String,
        )

        @Serializable
        data class PackageDependency(
            val type: String,
            val path: String? = null,
        )
    }

    private fun describeLocalPackage(path: File): PackageDescription {
        val jsonBuffer = ByteArrayOutputStream()
        execOps.exec { exec ->
            exec.workingDir(path)
            exec.standardOutput = jsonBuffer
            exec.commandLine("swift", "package", "describe", "--type", "json")
            exec.environment.keys.filter {
                // Swift CLIs try to compile the manifest for iphonesimulator... with these envs
                it.startsWith("SDK")
            }.forEach {
                exec.environment.remove(it)
            }
        }
        return packageDescriptionJson.decodeFromStream<PackageDescription>(ByteArrayInputStream(jsonBuffer.toByteArray()))
    }

    private fun findLocalPackageSources(path: File, packageDescription: PackageDescription): List<File> {
        return packageDescription.targets.filter {
            (it.moduleType == "SwiftTarget" || it.moduleType == "ClangTarget") && it.type != "test"
        }.map {
            path.resolve(it.path)
        }
    }

    private fun findTransitiveLocalPackages(packageDescription: PackageDescription): List<File> {
        return packageDescription.dependencies.mapNotNull { dependency ->
            dependency.path?.takeIf {
                dependency.type == FILE_SYSTEM_DEPENDENCY_TYPE
            }?.let(::File)
        }
    }

    companion object {
        const val TASK_NAME = "computeLocalPackageDependencyInputFiles"
        private const val MANIFEST_FILE_NAME = "Package.swift"
        private const val FILE_SYSTEM_DEPENDENCY_TYPE = "fileSystem"
        private val packageDescriptionJson = Json {
            ignoreUnknownKeys = true
        }
    }
}