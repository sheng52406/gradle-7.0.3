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

package com.android.build.api.variant

import com.android.build.gradle.internal.dsl.CoreExternalNativeNdkBuildOptions
import com.android.build.gradle.internal.services.VariantPropertiesApiServices
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty

class ExternalNdkBuildImpl(
    mergedExternalNativeNdkBuildOptions: CoreExternalNativeNdkBuildOptions,
    variantPropertiesApiServices: VariantPropertiesApiServices
): ExternalNativeBuild {

    override val abiFilters: SetProperty<String> =
            variantPropertiesApiServices.setPropertyOf(
                type = String::class.java,
                value = mergedExternalNativeNdkBuildOptions.abiFilters,
                disallowUnsafeRead = false, // see b/193722661
            )

    override val arguments: ListProperty<String> =
            variantPropertiesApiServices.listPropertyOf(
                type = String::class.java,
                value = mergedExternalNativeNdkBuildOptions.arguments,
                disallowUnsafeRead = false, // see b/193722661
            )

    override val cFlags: ListProperty<String> =
            variantPropertiesApiServices.listPropertyOf(
                type = String::class.java,
                value = mergedExternalNativeNdkBuildOptions.getcFlags(),
                disallowUnsafeRead = false, // see b/193722661
            )

    override val cppFlags: ListProperty<String> =
            variantPropertiesApiServices.listPropertyOf(
                type = String::class.java,
                value = mergedExternalNativeNdkBuildOptions.cppFlags,
                disallowUnsafeRead = false, // see b/193722661
            )

    override val targets: SetProperty<String> =
            variantPropertiesApiServices.setPropertyOf(
                type = String::class.java,
                value = mergedExternalNativeNdkBuildOptions.targets,
                disallowUnsafeRead = false, // see b/193722661
            )
}
