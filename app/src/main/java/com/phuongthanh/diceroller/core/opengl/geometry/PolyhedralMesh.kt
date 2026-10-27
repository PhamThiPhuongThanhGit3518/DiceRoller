package com.phuongthanh.diceroller.core.opengl.geometry

import com.phuongthanh.diceroller.core.opengl.math.Quaternion
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Encapsulates 3D mesh data ready for OpenGL ES drawing.
 * Vertex format: Position (x, y, z) + Normal (nx, ny, nz) + UV (u, v) = 8 floats per vertex.
 */
class PolyhedralMesh(
    val vertexBuffer: FloatBuffer,
    val vertexCount: Int,
    val faceCount: Int,
    val targetRotations: Map<Int, Quaternion>
) {
    companion object {
        const val FLOATS_PER_VERTEX = 8
        const val BYTES_PER_FLOAT = 4
        const val STRIDE_BYTES = FLOATS_PER_VERTEX * BYTES_PER_FLOAT

        const val POSITION_OFFSET = 0
        const val POSITION_SIZE = 3

        const val NORMAL_OFFSET = 3 * BYTES_PER_FLOAT
        const val NORMAL_SIZE = 3

        const val TEXCOORD_OFFSET = 6 * BYTES_PER_FLOAT
        const val TEXCOORD_SIZE = 2

        fun createFloatBuffer(data: FloatArray): FloatBuffer {
            return ByteBuffer.allocateDirect(data.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply {
                    put(data)
                    position(0)
                }
        }
    }
}
