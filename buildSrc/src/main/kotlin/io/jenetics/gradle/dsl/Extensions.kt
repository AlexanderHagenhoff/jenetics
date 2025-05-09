/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.gradle.dsl

import io.jenetics.gradle.Constants.SNIPPET_DIR
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceSetOutput
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since 8.1
 * @version 8.1
 */

/**
 * Gets the main source set of the project.
 */
val SourceSetContainer.main: SourceSet
	get() = this.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

/**
 * Gets all source sets for java projects.
 */
val Project.sourceSets: SourceSetContainer
	get() = this.extensions.getByType(JavaPluginExtension::class.java).sourceSets

fun Project.testClasses(projectName: String): SourceSetOutput {
	return this.rootProject.project(projectName).sourceSets["test"].output
}

/**
 * Gets the module name of the project, as configured in the build file.
 */
var Project.moduleName: String
	get() = if (this.isModule) this.extra.get("moduleName").toString()
			else this.name
	set(value) = this.extra.set("moduleName", value)

/**
 * Checks if the project is configured as a module.
 */
val Project.isModule: Boolean
	get() = this.extra.has("moduleName")

/**
 * Gets all Java sources of the project.
 */
val Project.allJava: FileTree
	get() = this.sourceSets.main.allJava.asFileTree

/**
 * Gets all Java sources from a list of projects.
 */
val List<Project>.allJava: FileTree
	get() = this.map { it.allJava }.reduce { a, b -> a.plus(b) }

/**
 * Gets the classpath of the main source set of the project.
 */
val Project.compileClasspath: FileCollection
	get() = this.sourceSets.main.compileClasspath

/**
 * Gets the combined classpath from a list of projects.
 */
val List<Project>.compileClasspath: FileCollection
	get() = this.map { it.compileClasspath }.reduce { a, b -> a.plus(b) }

/**
 * Gets all source directories of the project.
 */
val Project.sourceDirs: List<File>
	get() = this.sourceSets.flatMap { it.allSource.sourceDirectories }

/**
 * Gets the _snippet_ paths fo a project.
 */
val Project.snippetPaths: Set<String>
	get() = this.sourceDirs
		.flatMap { dir ->
			dir.walk()
				.filter { file -> file.isDirectory && file.endsWith(SNIPPET_DIR) }
				.map { it.absolutePath }
		}
		.toSet()

/**
 * Gets the list of defined snippet files.
 */
val Project.snippetFiles: List<String>
	get() = this.snippetPaths
		.flatMap { dir -> Files.list(Path.of(dir)).toList() }
		.filter { it.isRegularFile() }
		.map { file -> file.toAbsolutePath().toString() }
		.toList()

/**
 * Gets the list of snippet classes, including the path.
 */
val Project.snippetClasses: List<String>
	get() = this.snippetFiles
		.map { file ->
			val index = file.lastIndexOf("/src/main/java/")
			if (index != -1) {
				file.substring(index + "/src/main/java/".length)
					.replace("/$SNIPPET_DIR", "")
					.replace(".java", "")
			} else {
				file
			}
		}
		.toList()

/**
 * Gets the _snippet_ path of a project as string.
 */
val Project.snippetPathString: String?
	get() = this.snippetPaths
		.joinToString(separator = File.pathSeparator)
		.ifEmpty { null }

/**
 * Gets the _snippet_ path of a list of projects as string.
 */
val List<Project>.snippetPathString: String?
	get() = this.flatMap { it.snippetPaths }
		.joinToString(separator = File.pathSeparator)
		.ifEmpty { null }

