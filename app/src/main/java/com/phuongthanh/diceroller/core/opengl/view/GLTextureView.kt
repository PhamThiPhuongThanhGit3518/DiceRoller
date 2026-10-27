package com.phuongthanh.diceroller.core.opengl.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.View
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10

/**
 * A TextureView implementation that manages an OpenGL ES 2.0 rendering thread,
 * providing the same API interface as GLSurfaceView while seamlessly integrating
 * into the Android View hierarchy (supporting animations, transparency, INVISIBLE/GONE,
 * and eliminating black flickering/hole punching).
 */
class GLTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {

    companion object {
        const val RENDERMODE_WHEN_DIRTY = 0
        const val RENDERMODE_CONTINUOUSLY = 1
        private const val TARGET_FRAME_DURATION_MS = 16L // ~60 FPS cap
        private const val TAG = "GLTextureView"
    }

    private var renderer: GLSurfaceView.Renderer? = null
    var renderMode: Int = RENDERMODE_CONTINUOUSLY

    private var glThread: GLThread? = null

    init {
        surfaceTextureListener = this
        isOpaque = false
    }

    fun setRenderer(renderer: GLSurfaceView.Renderer) {
        check(this.renderer == null) { "Renderer has already been set" }
        this.renderer = renderer
        startThreadIfNeeded()
    }

    fun requestRender() {
        glThread?.requestRender()
    }

    fun onResume() {
        glThread?.onResume()
    }

    fun onPause() {
        glThread?.onPause()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startThreadIfNeeded()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopThread()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        glThread?.onVisibilityChanged(isShown)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        glThread?.onVisibilityChanged(isShown)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun startThreadIfNeeded() {
        val r = renderer ?: return
        if (glThread == null || !glThread!!.isAlive) {
            glThread = GLThread(
                renderer = r,
                renderModeProvider = { renderMode },
                onFrameRendered = {}
            ).apply {
                onVisibilityChanged(this@GLTextureView.isShown)
                start()
            }
        }
    }

    private fun stopThread() {
        glThread?.let {
            it.requestExitAndWait()
            glThread = null
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        surface.setDefaultBufferSize(width, height)
        startThreadIfNeeded()
        glThread?.onSurfaceAvailable(surface, width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        surface.setDefaultBufferSize(width, height)
        glThread?.onSurfaceSizeChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        glThread?.onSurfaceDestroyedAndWait()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // No-op
    }

    private class GLThread(
        private val renderer: GLSurfaceView.Renderer,
        private val renderModeProvider: () -> Int,
        private val onFrameRendered: () -> Unit
    ) : Thread("GLTextureViewThread") {

        private val lock = Object()
        private val eglHelper = EglHelper()

        private var surfaceTexture: SurfaceTexture? = null
        private var width: Int = 0
        private var height: Int = 0

        private var isRunning = true
        private var isPaused = false
        private var isVisible = true
        private var hasSurface = false
        private var hasNewSurface = false
        private var sizeChanged = false
        private var renderRequested = true
        private var destroySurfaceRequested = false

        override fun run() {
            try {
                eglHelper.start()
                var surfaceCreatedDone = false

                while (true) {
                    var w = 0
                    var h = 0
                    var needSizeChanged = false
                    var needRender = false

                    synchronized(lock) {
                        while (true) {
                            if (!isRunning) return

                            if (destroySurfaceRequested) {
                                eglHelper.destroySurface()
                                destroySurfaceRequested = false
                                hasSurface = false
                                surfaceCreatedDone = false
                                lock.notifyAll()
                            }

                            if (hasNewSurface) {
                                val st = surfaceTexture
                                if (st != null) {
                                    if (eglHelper.createSurface(st)) {
                                        hasSurface = true
                                        hasNewSurface = false
                                        sizeChanged = true
                                        surfaceCreatedDone = false
                                        lock.notifyAll()
                                    }
                                }
                            }

                            val canRender = !isPaused && isVisible && hasSurface &&
                                (renderModeProvider() == RENDERMODE_CONTINUOUSLY || renderRequested)

                            if (canRender && width > 0 && height > 0) {
                                w = width
                                h = height
                                if (sizeChanged) {
                                    needSizeChanged = true
                                    sizeChanged = false
                                }
                                needRender = true
                                renderRequested = false
                                break
                            }

                            lock.wait()
                        }
                    }

                    if (hasSurface && w > 0 && h > 0) {
                        if (!surfaceCreatedDone) {
                            renderer.onSurfaceCreated(eglHelper.gl, eglHelper.getConfig())
                            surfaceCreatedDone = true
                        }

                        if (needSizeChanged) {
                            renderer.onSurfaceChanged(eglHelper.gl, w, h)
                        }

                        if (needRender) {
                            val startTime = System.currentTimeMillis()
                            renderer.onDrawFrame(eglHelper.gl)
                            eglHelper.swapBuffers()
                            onFrameRendered()

                            val elapsed = System.currentTimeMillis() - startTime
                            if (elapsed < TARGET_FRAME_DURATION_MS) {
                                try {
                                    sleep(TARGET_FRAME_DURATION_MS - elapsed)
                                } catch (_: InterruptedException) {
                                    break
                                }
                            }
                        }
                    }
                }
            } catch (_: InterruptedException) {
                // Thread interrupted on shutdown
            } catch (e: Exception) {
                Log.e(TAG, "GLThread encountered error", e)
            } finally {
                synchronized(lock) {
                    eglHelper.finish()
                }
            }
        }

        fun onSurfaceAvailable(surface: SurfaceTexture, w: Int, h: Int) {
            synchronized(lock) {
                this.surfaceTexture = surface
                this.width = w
                this.height = h
                this.hasNewSurface = true
                lock.notifyAll()
            }
        }

        fun onSurfaceSizeChanged(w: Int, h: Int) {
            synchronized(lock) {
                this.width = w
                this.height = h
                this.sizeChanged = true
                this.renderRequested = true
                lock.notifyAll()
            }
        }

        fun onSurfaceDestroyedAndWait() {
            synchronized(lock) {
                destroySurfaceRequested = true
                surfaceTexture = null
                lock.notifyAll()

                while (destroySurfaceRequested && isRunning && isAlive) {
                    try {
                        lock.wait(100)
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            }
        }

        fun onPause() {
            synchronized(lock) {
                isPaused = true
                lock.notifyAll()
            }
        }

        fun onResume() {
            synchronized(lock) {
                isPaused = false
                lock.notifyAll()
            }
        }

        fun onVisibilityChanged(visible: Boolean) {
            synchronized(lock) {
                isVisible = visible
                lock.notifyAll()
            }
        }

        fun requestRender() {
            synchronized(lock) {
                renderRequested = true
                lock.notifyAll()
            }
        }

        fun requestExitAndWait() {
            synchronized(lock) {
                isRunning = false
                destroySurfaceRequested = true
                lock.notifyAll()
            }
            try {
                join(500)
            } catch (_: InterruptedException) {}
        }
    }

    private class EglHelper {
        private var egl: EGL10? = null
        private var eglDisplay: EGLDisplay? = null
        private var eglConfig: EGLConfig? = null
        private var eglContext: EGLContext? = null
        private var eglSurface: EGLSurface? = null
        var gl: GL10? = null
            private set

        fun start() {
            val egl = EGLContext.getEGL() as EGL10
            this.egl = egl

            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            if (display == EGL10.EGL_NO_DISPLAY) {
                throw RuntimeException("eglGetDisplay failed: ${egl.eglGetError()}")
            }
            this.eglDisplay = display

            val version = IntArray(2)
            if (!egl.eglInitialize(display, version)) {
                throw RuntimeException("eglInitialize failed: ${egl.eglGetError()}")
            }

            val configAttribsList = listOf(
                intArrayOf(
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_NONE
                ),
                intArrayOf(
                    EGL10.EGL_RED_SIZE, 5,
                    EGL10.EGL_GREEN_SIZE, 6,
                    EGL10.EGL_BLUE_SIZE, 5,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_RENDERABLE_TYPE, 4,
                    EGL10.EGL_NONE
                )
            )

            var chosenConfig: EGLConfig? = null
            for (attribs in configAttribsList) {
                val configs = arrayOfNulls<EGLConfig>(1)
                val numConfigs = IntArray(1)
                if (egl.eglChooseConfig(display, attribs, configs, 1, numConfigs) && numConfigs[0] > 0) {
                    chosenConfig = configs[0]
                    break
                }
            }
            if (chosenConfig == null) {
                throw RuntimeException("eglChooseConfig failed: ${egl.eglGetError()}")
            }
            this.eglConfig = chosenConfig

            val contextAttribs = intArrayOf(
                0x3098, 2, // EGL_CONTEXT_CLIENT_VERSION = 2
                EGL10.EGL_NONE
            )
            val context = egl.eglCreateContext(display, chosenConfig, EGL10.EGL_NO_CONTEXT, contextAttribs)
            if (context == null || context == EGL10.EGL_NO_CONTEXT) {
                throw RuntimeException("eglCreateContext failed: ${egl.eglGetError()}")
            }
            this.eglContext = context
        }

        fun createSurface(surfaceTexture: SurfaceTexture): Boolean {
            val egl = this.egl ?: return false
            val display = this.eglDisplay ?: return false
            val config = this.eglConfig ?: return false
            val context = this.eglContext ?: return false

            destroySurface()

            val surface = egl.eglCreateWindowSurface(display, config, surfaceTexture, null)
            if (surface == null || surface == EGL10.EGL_NO_SURFACE) {
                return false
            }
            this.eglSurface = surface

            if (!egl.eglMakeCurrent(display, surface, surface, context)) {
                return false
            }

            gl = context.gl as GL10
            return true
        }

        fun swapBuffers(): Boolean {
            val egl = this.egl ?: return false
            val display = this.eglDisplay ?: return false
            val surface = this.eglSurface ?: return false

            return egl.eglSwapBuffers(display, surface)
        }

        fun destroySurface() {
            val egl = this.egl ?: return
            val display = this.eglDisplay ?: return
            val surface = this.eglSurface

            if (surface != null && surface != EGL10.EGL_NO_SURFACE) {
                egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
                egl.eglDestroySurface(display, surface)
                this.eglSurface = null
            }
        }

        fun finish() {
            destroySurface()
            val egl = this.egl ?: return
            val display = this.eglDisplay ?: return
            val context = this.eglContext

            if (context != null && context != EGL10.EGL_NO_CONTEXT) {
                egl.eglDestroyContext(display, context)
                this.eglContext = null
            }
            egl.eglTerminate(display)
            this.eglDisplay = null
        }

        fun getConfig(): EGLConfig? = eglConfig
    }
}
