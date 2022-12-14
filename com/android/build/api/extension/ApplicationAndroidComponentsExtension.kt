/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.build.api.extension

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.ApplicationVariantBuilder

@Deprecated(
    message= "Use the com.android.build.api.variant package",
    replaceWith = ReplaceWith(
        "ApplicationAndroidComponentsExtension",
        "com.android.build.api.variant.ApplicationAndroidComponentsExtension"
    ),
    level = DeprecationLevel.WARNING
)
interface ApplicationAndroidComponentsExtension:
    AndroidComponentsExtension<ApplicationExtension, ApplicationVariantBuilder, ApplicationVariant>
