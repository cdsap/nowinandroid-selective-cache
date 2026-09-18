/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Added for the selective-remote-build-cache demo. Develocity provides the remote cache this
// plugin filters in front of; the filter itself is applied below, in buildCache.
plugins {
    id("com.gradle.develocity") version "4.5.1"
    id("io.github.cdsap.selective-remote-cache") version "0.1.1"
}

val isCi = !System.getenv("CI").isNullOrEmpty()

develocity {
    server = "https://ge.solutions-team.gradle.com"
    buildScan {
        // Credentials come from the DEVELOCITY_ACCESS_KEY environment variable.
        uploadInBackground = !isCi
        tag(if (isCi) "CI" else "local")
        tag("selective-remote-cache")
    }
}

buildCache {
    local {
        isEnabled = true
        isPush = true
    }

    // The point of this repository: dex merging is held back from the REMOTE cache only. Those
    // entries are large and usually cheaper to recompute than to pull over a WAN; every other
    // cacheable task keeps using the remote as normal, and the local tier is untouched.
    remote(io.github.cdsap.selectivecache.SelectiveRemoteBuildCache::class.java) {
        isPush = isCi
        excludedTypes = setOf("com.android.build.gradle.internal.tasks.DexMergingTask")
        debug = true
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}
rootProject.name = "nowinandroid-selective-cache"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":app-nia-catalog")
include(":benchmarks")
include(":core:analytics")
include(":core:common")
include(":core:data")
include(":core:data-test")
include(":core:database")
include(":core:datastore")
include(":core:datastore-proto")
include(":core:datastore-test")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:navigation")
include(":core:network")
include(":core:notifications")
include(":core:screenshot-testing")
include(":core:testing")
include(":core:ui")

include(":feature:foryou:api")
include(":feature:foryou:impl")
include(":feature:interests:api")
include(":feature:interests:impl")
include(":feature:bookmarks:api")
include(":feature:bookmarks:impl")
include(":feature:topic:api")
include(":feature:topic:impl")
include(":feature:search:api")
include(":feature:search:impl")
include(":feature:settings:impl")
include(":lint")
include(":sync:work")
include(":sync:sync-test")
include(":ui-test-hilt-manifest")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Now in Android requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
