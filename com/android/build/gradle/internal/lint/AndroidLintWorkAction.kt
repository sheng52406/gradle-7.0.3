/*
 * Copyright (C) 2020 The Android Open Source Project
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
 */

package com.android.build.gradle.internal.lint

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.net.URI
import java.net.URLClassLoader

@Suppress("UnstableApiUsage")
abstract class AndroidLintWorkAction : WorkAction<AndroidLintWorkAction.LintWorkActionParameters> {

    abstract class LintWorkActionParameters : WorkParameters {
        abstract val mainClass: Property<String>
        abstract val arguments: ListProperty<String>
        abstract val classpath: ConfigurableFileCollection
        abstract val android: Property<Boolean>
        abstract val fatalOnly: Property<Boolean>
        abstract val runInProcess: Property<Boolean>
    }

    override fun execute() {
        val logger = Logging.getLogger(this.javaClass)
        val arguments = parameters.arguments.get()
        logger.debug("Running lint " + arguments.joinToString(" "))
        if (!parameters.runInProcess.get()) {
            logger.info(
                "Max memory for Android Lint: {}m\n(can be configured by {}=2G in gradle.properties)",
                Runtime.getRuntime().maxMemory() / 1024 / 1024,
                com.android.build.gradle.options.StringOption.LINT_HEAP_SIZE.propertyName
            )
        }
        val execResult = runLint(arguments)
        logger.debug("Lint returned $execResult")
        if (execResult == ERRNO_SUCCESS) {
            return
        }

        val message: String = when {
            !parameters.android.get() -> {
                """
                    Lint found errors in the project; aborting build.

                    Fix the issues identified by lint, or add the following to your build script to proceed with errors:
                    ...
                    lintOptions {
                        abortOnError false
                    }
                    ...
                    """.trimIndent()
            }
            parameters.fatalOnly.get() -> {
                """
                    Lint found fatal errors while assembling a release target.

                    To proceed, either fix the issues identified by lint, or modify your build script as follows:
                    ...
                    android {
                        lintOptions {
                            checkReleaseBuilds false
                            // Or, if you prefer, you can continue to check for errors in release builds,
                            // but continue the build even when errors are found:
                            abortOnError false
                        }
                    }
                    ...
                    """.trimIndent()
            }
            else -> {
                """
                    Lint found errors in the project; aborting build.

                    Fix the issues identified by lint, or add the following to your build script to proceed with errors:
                    ...
                    android {
                        lintOptions {
                            abortOnError false
                        }
                    }
                    ...
                    """.trimIndent()
            }
        }

        when (execResult) {
            ERRNO_ERRORS -> {
                logger.error(message)
                throw RuntimeException(
                    if (parameters.android.get() && parameters.fatalOnly.get()) {
                        "Lint found fatal errors while assembling a release target."
                    } else {
                        "Lint found errors in the project; aborting build."
                    }
                )
            }
            ERRNO_USAGE -> throw IllegalStateException("Internal Error: Unexpected lint usage")
            ERRNO_EXISTS -> throw RuntimeException("Unable to write lint output")
            ERRNO_HELP -> throw IllegalStateException("Internal error: Unexpected lint help call")
            ERRNO_INVALID_ARGS -> throw IllegalStateException("Internal error: Unexpected lint invalid arguments")
            ERRNO_CREATED_BASELINE -> throw RuntimeException("Aborting build since new baseline file was created")
            ERRNO_APPLIED_SUGGESTIONS -> throw RuntimeException("Aborting build since sources were modified to apply quickfixes after compilation")
            else -> throw IllegalStateException("Internal error: unexpected lint return value ${execResult}")
        }
    }

    private fun runLint(arguments: List<String>): Int {
        val classLoader = getClassloader(parameters.classpath)
        return invokeLintMainRunMethod(classLoader, arguments)
    }

    private fun invokeLintMainRunMethod(classLoader: ClassLoader, arguments: List<String>): Int {
        // val returnValue: Int = Main().run(arguments.toTypedArray())
        val cls = classLoader.loadClass(parameters.mainClass.get())
        val method = cls.getMethod("run", Array<String>::class.java)
        val lintMain = cls.getDeclaredConstructor().newInstance()
        val returnValue = method.invoke(lintMain, arguments.toTypedArray()) as Int

        // If running lint in process, we call dispose() via LintClassLoaderBuildService.close()
        // after all lint invocations; otherwise, we call dispose() here.
        if (!parameters.runInProcess.get()) {
            dispose()
        }

        return returnValue
    }


    companion object {

        private const val ERRNO_SUCCESS = 0
        private const val ERRNO_ERRORS = 1
        private const val ERRNO_USAGE = 2
        private const val ERRNO_EXISTS = 3
        private const val ERRNO_HELP = 4
        private const val ERRNO_INVALID_ARGS = 5
        private const val ERRNO_CREATED_BASELINE = 6
        private const val ERRNO_APPLIED_SUGGESTIONS = 7

        private var _cachedClassLoader: URLClassLoader? = null
        private var cachedClassLoaderUris: List<URI> = listOf()

        @Synchronized
        private fun getClassloader(classpath: FileCollection): ClassLoader {
            val uris = classpath.files.map { it.toURI() }
            if (uris != cachedClassLoaderUris) {
                cachedClassLoaderUris = uris
                _cachedClassLoader = createClassLoader(uris)
            }
            return _cachedClassLoader!!
        }

        private fun createClassLoader(classpath: List<URI>): URLClassLoader {
            val classpathUrls = classpath.map { it.toURL() }.toTypedArray()
            return URLClassLoader(classpathUrls, getPlatformClassLoader())
        }

        private fun getPlatformClassLoader(): ClassLoader {
            // AGP is currently compiled against java 8 APIs, so do this by reflection (b/160392650)
            return ClassLoader::class.java.getMethod("getPlatformClassLoader").invoke(null) as ClassLoader
        }

        @Synchronized
        fun dispose() {
            _cachedClassLoader?.loadClass("com.android.tools.lint.UastEnvironment")
                ?.getDeclaredMethod("disposeApplicationEnvironment")
                ?.invoke(null)
        }
    }
}
