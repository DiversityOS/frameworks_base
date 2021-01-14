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

package android.media.metrics;

import android.annotation.NonNull;

/**
 * Interface for playback related components used by playback metrics.
 * @hide
 */
public interface PlaybackComponent {

    /**
     * Sets the playback ID of the component.
     */
    void setPlaybackId(@NonNull String playbackId);

    /**
     * Gets playback ID.
     */
    @NonNull String getPlaybackId();
}
