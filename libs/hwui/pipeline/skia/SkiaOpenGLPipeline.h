/*
 * Copyright (C) 2016 The Android Open Source Project
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

#pragma once

#include "SkiaPipeline.h"

#include "renderstate/RenderState.h"

namespace android {

class Bitmap;

namespace uirenderer {
namespace skiapipeline {

class SkiaOpenGLPipeline : public SkiaPipeline, public IGpuContextCallback {
public:
    SkiaOpenGLPipeline(renderthread::RenderThread& thread);
    virtual ~SkiaOpenGLPipeline();

    renderthread::MakeCurrentResult makeCurrent() override;
    renderthread::Frame getFrame() override;
    bool draw(const renderthread::Frame& frame, const SkRect& screenDirty, const SkRect& dirty,
              const LightGeometry& lightGeometry, LayerUpdateQueue* layerUpdateQueue,
              const Rect& contentDrawBounds, bool opaque, const LightInfo& lightInfo,
              const std::vector<sp<RenderNode> >& renderNodes,
              FrameInfoVisualizer* profiler) override;
    bool swapBuffers(const renderthread::Frame& frame, bool drew, const SkRect& screenDirty,
                     FrameInfo* currentFrameInfo, bool* requireSwap) override;
    DeferredLayerUpdater* createTextureLayer() override;
    bool setSurface(Surface* window, renderthread::SwapBehavior swapBehavior,
                    renderthread::ColorMode colorMode) override;
    void onStop() override;
    bool isSurfaceReady() override;
    bool isContextReady() override;
    SkColorType getSurfaceColorType() const override;
    sk_sp<SkColorSpace> getSurfaceColorSpace() override;

    static void invokeFunctor(const renderthread::RenderThread& thread, Functor* functor);

protected:
    void onContextDestroyed() override;

private:
    renderthread::EglManager& mEglManager;
    EGLSurface mEglSurface = EGL_NO_SURFACE;
    bool mBufferPreserved = false;
};

} /* namespace skiapipeline */
} /* namespace uirenderer */
} /* namespace android */
