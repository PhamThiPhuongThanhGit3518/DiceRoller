package com.phuongthanh.diceroller.core.opengl.view

import android.graphics.Color
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.core.opengl.geometry.PolyhedralGeometryFactory
import com.phuongthanh.diceroller.core.opengl.geometry.PolyhedralMesh
import com.phuongthanh.diceroller.core.opengl.model.Dice3DState
import com.phuongthanh.diceroller.core.opengl.shader.DiceShader
import com.phuongthanh.diceroller.core.opengl.texture.DiceTextureGenerator
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Dice3DRenderer(
    private val onAnimationComplete: () -> Unit = {}
) : GLSurfaceView.Renderer {

    companion object {
        /**
         * Kích thước scale của xúc xắc 3D theo số lượng xúc xắc hiển thị:
         * - SCALE_1_DICE: Khi có 1 xúc xắc (mặc định 0.95f)
         * - SCALE_2_DICE: Khi có 2 xúc xắc trên 1 hàng (mặc định 0.78f)
         * - SCALE_3_DICE: Khi có 3 xúc xắc trên 1 hàng (mặc định 0.65f)
         * - SCALE_4_DICE: Khi có 4 xúc xắc lưới 2x2 (mặc định 0.58f)
         * - SCALE_MANY_DICE: Khi có từ 5 xúc xắc trở lên (mặc định 0.52f)
         */
        const val SCALE_1_DICE = 0.85f
        const val SCALE_2_DICE = 0.8f
        const val SCALE_3_DICE = 0.72f
        const val SCALE_4_DICE = 0.72f
        const val SCALE_MANY_DICE = 0.72f
    }

    private val shader = DiceShader()
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    // Per-context texture cache ensuring isolation across multiple concurrent OpenGL contexts
    private val textureCache = mutableMapOf<Long, Int>()

    @Volatile
    var diceStates: List<Dice3DState> = emptyList()

    @Volatile
    var partitionRects: List<RectF> = emptyList()

    @Volatile
    var surfaceWidth: Int = 0

    @Volatile
    var surfaceHeight: Int = 0

    @Volatile
    var diceColor: Int = 0xFFC4EA7E.toInt()

    @Volatile
    var dotColor: Int = 0xFFFFFFFF.toInt()

    @Volatile
    var sameColorsAllDice: Boolean = true

    @Volatile
    var faceStyle: DiceFaceStyle = DiceFaceStyle.DOTS

    // Elegant deep contrasting tabletop / board background color (not matching the dice)
    @Volatile
    var tableBgColor: Int = 0x00000000 // Default transparent

    @Volatile
    var tableSeparatorColor: Int = 0x00000000

    @Volatile
    var showDividers: Boolean = false

    @Volatile
    var customScale1Die: Float = SCALE_1_DICE

    @Volatile
    var customScale2Dice: Float = SCALE_2_DICE

    @Volatile
    var customScale3Dice: Float = SCALE_3_DICE

    fun getDiceScale(count: Int): Float {
        return when (count) {
            1 -> customScale1Die
            2 -> customScale2Dice
            3 -> customScale3Dice
            4 -> SCALE_4_DICE
            else -> SCALE_MANY_DICE
        }
    }

    private val dicePalette = listOf(
        0xFFE53935.toInt(), // Red (matches Figma 3D die 1)
        0xFF0075FF.toInt(), // Royal Blue (matches Figma 3D die 2)
        0xFF10B981.toInt(), // Emerald Green (matches Figma 3D die 3)
        0xFFF59E0B.toInt(), // Amber / Gold
        0xFF8B5CF6.toInt(), // Purple
        0xFF06B6D4.toInt()  // Cyan
    )

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var lastFrameTimeNanos: Long = 0L
    private var wasRollingLastFrame: Boolean = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Backface culling ensures only front-facing polygons are visible
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
        GLES20.glFrontFace(GLES20.GL_CCW)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Texture IDs from previous EGLContext are now invalid
        textureCache.clear()

        shader.init()
        lastFrameTimeNanos = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
    }

    private fun getOrCreateTexture(diceColor: Int, dotColor: Int, style: DiceFaceStyle): Int {
        val styleBit = (style.ordinal.toLong() and 0x3L) shl 60
        val key = (diceColor.toLong() shl 32) or (dotColor.toLong() and 0xFFFFFFFFL) xor styleBit
        val cached = textureCache[key]
        if (cached != null && cached != 0) {
            return cached
        }

        val bitmap = DiceTextureGenerator.getAtlasBitmap(diceColor, dotColor, style)

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        textureCache[key] = textureId
        return textureId
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentStates = diceStates
        val currentRects = partitionRects
        val w = surfaceWidth
        val h = surfaceHeight

        if (w <= 0 || h <= 0 || currentStates.isEmpty()) {
            val r = Color.red(tableBgColor) / 255f
            val g = Color.green(tableBgColor) / 255f
            val b = Color.blue(tableBgColor) / 255f
            GLES20.glClearColor(r, g, b, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            return
        }

        val now = System.nanoTime()
        val deltaSeconds = if (lastFrameTimeNanos > 0L) {
            ((now - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        } else {
            0.016f
        }
        lastFrameTimeNanos = now

        // Update physics for all dice
        var anyRolling = false
        for (die in currentStates) {
            val rolling = die.update(deltaSeconds)
            if (rolling) anyRolling = true
        }

        if (wasRollingLastFrame && !anyRolling) {
            mainHandler.post { onAnimationComplete() }
        }
        wasRollingLastFrame = anyRolling

        // 1. Clear full canvas with separator border color or table background
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
        val clearColor = if (showDividers) tableSeparatorColor else tableBgColor
        val clearA = Color.alpha(clearColor) / 255f
        val clearR = Color.red(clearColor) / 255f
        val clearG = Color.green(clearColor) / 255f
        val clearB = Color.blue(clearColor) / 255f
        GLES20.glClearColor(clearR, clearG, clearB, clearA)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)

        shader.use()

        val inset = if (showDividers) 2f else 0f
        val renderCount = minOf(currentStates.size, currentRects.size)
        val diceScale = getDiceScale(renderCount)

        for (i in 0 until renderCount) {
            val die = currentStates[i]
            val rect = currentRects[i]

            val vx = (rect.left + inset).toInt()
            val vy = (h - rect.bottom + inset).toInt()
            val vw = (rect.width() - inset * 2f).toInt()
            val vh = (rect.height() - inset * 2f).toInt()

            if (vw <= 0 || vh <= 0) continue

            GLES20.glViewport(vx, vy, vw, vh)
            GLES20.glScissor(vx, vy, vw, vh)

            // 2. Clear depth buffer for this die (and color buffer if dividers are shown)
            if (showDividers) {
                val bgA = Color.alpha(tableBgColor) / 255f
                val bgR = Color.red(tableBgColor) / 255f
                val bgG = Color.green(tableBgColor) / 255f
                val bgB = Color.blue(tableBgColor) / 255f
                GLES20.glClearColor(bgR, bgG, bgB, bgA)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            } else {
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
            }

            // 3. Setup Camera & Projection
            val aspect = vw.toFloat() / vh.toFloat()
            val fovY = if (renderCount >= 2) {
                // Khóa góc nhìn ngang (horizontal FOV) cho lưới 3 cột (>= 2 xúc xắc)
                // để khi giảm chiều cao hàng (cellH), các hàng xích lại gần nhau mà xúc xắc không bị teo nhỏ
                val referenceAspect = 1f / 1.35f
                val targetTanFovX = Math.tan(Math.toRadians(42.0 / 2.0)) * referenceAspect
                (2.0 * Math.toDegrees(Math.atan(targetTanFovX / aspect))).toFloat()
            } else {
                42f
            }
            Matrix.perspectiveM(projMatrix, 0, fovY, aspect, 0.1f, 100f)
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3.8f, 0f, 0f, 0f, 0f, 1f, 0f)

            // 4. Model Transform
            die.currentRotation.toMatrix(modelMatrix, 0)
            Matrix.scaleM(modelMatrix, 0, diceScale, diceScale, diceScale)
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvMatrix, 0)

            GLES20.glUniformMatrix4fv(shader.uMVPMatrixLocation, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(shader.uMVMatrixLocation, 1, false, mvMatrix, 0)
            GLES20.glUniform3f(shader.uLightPosLocation, 1.5f, 2.8f, 4.2f)
            GLES20.glUniform1f(shader.uAmbientLocation, 0.58f)
            GLES20.glUniform1f(shader.uSpecularLocation, 0.22f)
            GLES20.glUniform1f(shader.uIsD6Location, if (die.type == DiceType.D6) 1f else 0f)

            // 5. Dice Color (separate from background)
            val dieColor = if (sameColorsAllDice) {
                diceColor
            } else {
                dicePalette[i % dicePalette.size]
            }

            // Texture
            val textureId = getOrCreateTexture(dieColor, dotColor, faceStyle)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(shader.uTextureLocation, 0)

            // Mesh - duplicate FloatBuffer view to ensure thread safety across concurrent renderers
            val mesh = PolyhedralGeometryFactory.getMesh(die.type)
            val vBuffer = mesh.vertexBuffer.duplicate()

            // Position attribute
            vBuffer.position(PolyhedralMesh.POSITION_OFFSET / PolyhedralMesh.BYTES_PER_FLOAT)
            GLES20.glVertexAttribPointer(
                shader.aPositionLocation,
                PolyhedralMesh.POSITION_SIZE,
                GLES20.GL_FLOAT,
                false,
                PolyhedralMesh.STRIDE_BYTES,
                vBuffer
            )
            GLES20.glEnableVertexAttribArray(shader.aPositionLocation)

            // Normal attribute
            vBuffer.position(PolyhedralMesh.NORMAL_OFFSET / PolyhedralMesh.BYTES_PER_FLOAT)
            GLES20.glVertexAttribPointer(
                shader.aNormalLocation,
                PolyhedralMesh.NORMAL_SIZE,
                GLES20.GL_FLOAT,
                false,
                PolyhedralMesh.STRIDE_BYTES,
                vBuffer
            )
            GLES20.glEnableVertexAttribArray(shader.aNormalLocation)

            // UV attribute
            vBuffer.position(PolyhedralMesh.TEXCOORD_OFFSET / PolyhedralMesh.BYTES_PER_FLOAT)
            GLES20.glVertexAttribPointer(
                shader.aTexCoordLocation,
                PolyhedralMesh.TEXCOORD_SIZE,
                GLES20.GL_FLOAT,
                false,
                PolyhedralMesh.STRIDE_BYTES,
                vBuffer
            )
            GLES20.glEnableVertexAttribArray(shader.aTexCoordLocation)

            // Draw 3D Polyhedron with backface culling
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.vertexCount)

            // Cleanup attributes
            GLES20.glDisableVertexAttribArray(shader.aPositionLocation)
            GLES20.glDisableVertexAttribArray(shader.aNormalLocation)
            GLES20.glDisableVertexAttribArray(shader.aTexCoordLocation)
        }
    }

    fun release() {
        if (textureCache.isNotEmpty()) {
            val textures = textureCache.values.toIntArray()
            GLES20.glDeleteTextures(textures.size, textures, 0)
            textureCache.clear()
        }
    }
}
