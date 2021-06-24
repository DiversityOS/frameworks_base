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

package com.android.server.wm.flicker.close

import android.app.Instrumentation
import android.platform.test.annotations.Presubmit
import android.view.Surface
import androidx.test.filters.FlakyTest
import androidx.test.platform.app.InstrumentationRegistry
import com.android.server.wm.flicker.FlickerBuilderProvider
import com.android.server.wm.flicker.FlickerTestParameter
import com.android.server.wm.flicker.dsl.FlickerBuilder
import com.android.server.wm.flicker.helpers.SimpleAppHelper
import com.android.server.wm.flicker.helpers.StandardAppHelper
import com.android.server.wm.flicker.helpers.setRotation
import com.android.server.wm.flicker.launcherReplacesAppWindowAsTopWindow
import com.android.server.wm.flicker.navBarLayerIsVisible
import com.android.server.wm.flicker.navBarLayerRotatesAndScales
import com.android.server.wm.flicker.navBarWindowIsVisible
import com.android.server.wm.flicker.noUncoveredRegions
import com.android.server.wm.flicker.startRotation
import com.android.server.wm.flicker.statusBarLayerIsVisible
import com.android.server.wm.flicker.statusBarLayerRotatesScales
import com.android.server.wm.flicker.statusBarWindowIsVisible
import com.android.server.wm.flicker.launcherLayerReplacesApp
import com.android.server.wm.flicker.launcherWindowBecomesVisible
import org.junit.Test

abstract class CloseAppTransition(protected val testSpec: FlickerTestParameter) {
    protected val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    protected open val testApp: StandardAppHelper = SimpleAppHelper(instrumentation)
    protected open val transition: FlickerBuilder.(Map<String, Any?>) -> Unit = {
        setup {
            eachRun {
                testApp.launchViaIntent(wmHelper)
                this.setRotation(testSpec.config.startRotation)
            }
        }
        teardown {
            test {
                testApp.exit()
            }
        }
    }

    @FlickerBuilderProvider
    fun buildFlicker(): FlickerBuilder {
        return FlickerBuilder(instrumentation).apply {
            transition(testSpec.config)
        }
    }

    @Presubmit
    @Test
    open fun navBarWindowIsVisible() {
        testSpec.navBarWindowIsVisible()
    }

    @Presubmit
    @Test
    open fun statusBarWindowIsVisible() {
        testSpec.statusBarWindowIsVisible()
    }

    @FlakyTest
    @Test
    open fun navBarLayerIsVisible() {
        testSpec.navBarLayerIsVisible()
    }

    @Presubmit
    @Test
    open fun statusBarLayerIsVisible() {
        testSpec.statusBarLayerIsVisible()
    }

    @FlakyTest
    @Test
    open fun navBarLayerRotatesAndScales() {
        testSpec.navBarLayerRotatesAndScales(testSpec.config.startRotation, Surface.ROTATION_0)
    }

    @Presubmit
    @Test
    open fun statusBarLayerRotatesScales() {
        testSpec.statusBarLayerRotatesScales(testSpec.config.startRotation, Surface.ROTATION_0)
    }

    @Presubmit
    @Test
    open fun visibleWindowsShownMoreThanOneConsecutiveEntry() {
        testSpec.assertWm {
            this.visibleWindowsShownMoreThanOneConsecutiveEntry()
        }
    }

    @Presubmit
    @Test
    open fun visibleLayersShownMoreThanOneConsecutiveEntry() {
        testSpec.assertLayers {
            this.visibleLayersShownMoreThanOneConsecutiveEntry()
        }
    }

    @Presubmit
    @Test
    open fun noUncoveredRegions() {
        testSpec.noUncoveredRegions(testSpec.config.startRotation, Surface.ROTATION_0)
    }

    @Presubmit
    @Test
    open fun launcherReplacesAppWindowAsTopWindow() {
        testSpec.launcherReplacesAppWindowAsTopWindow(testApp)
    }

    @Presubmit
    @Test
    open fun launcherWindowBecomesVisible() {
        testSpec.launcherWindowBecomesVisible()
    }

    @Presubmit
    @Test
    open fun launcherLayerReplacesApp() {
        testSpec.launcherLayerReplacesApp(testApp)
    }
}