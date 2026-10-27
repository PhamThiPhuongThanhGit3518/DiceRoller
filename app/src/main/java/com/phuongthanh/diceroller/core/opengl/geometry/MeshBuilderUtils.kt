package com.phuongthanh.diceroller.core.opengl.geometry

import com.phuongthanh.diceroller.core.opengl.math.Quaternion
import com.phuongthanh.diceroller.core.opengl.math.Vector3
import kotlin.math.atan2

object MeshBuilderUtils {

    const val ATLAS_COLS = 6
    const val ATLAS_ROWS = 4

    fun getCellBounds(faceNumber: Int): FloatArray {
        val index = (faceNumber - 1).coerceAtLeast(0)
        val col = index % ATLAS_COLS
        val row = index / ATLAS_COLS

        val uMin = col.toFloat() / ATLAS_COLS.toFloat()
        val uMax = (col + 1).toFloat() / ATLAS_COLS.toFloat()

        val vMin = row.toFloat() / ATLAS_ROWS.toFloat()
        val vMax = (row + 1).toFloat() / ATLAS_ROWS.toFloat()

        return floatArrayOf(uMin, vMin, uMax, vMax)
    }

    /**
     * Maps 3D polygon vertices to 2D texture UV coordinates with ZERO distortion.
     * Uses conformal isometric projection from the polygon's local plane into the texture cell.
     */
    fun mapPlanarFaceUv(
        vertices: List<Vector3>,
        normal: Vector3,
        upVector: Vector3,
        faceNumber: Int
    ): List<Pair<Float, Float>> {
        var cx = 0f; var cy = 0f; var cz = 0f
        for (v in vertices) {
            cx += v.x; cy += v.y; cz += v.z
        }
        val count = vertices.size.toFloat()
        val centroid = Vector3(cx / count, cy / count, cz / count)

        // Orthonormal tangent basis in plane of face:
        // Up vector (U) points towards top of the number
        val uNorm = (upVector - normal * upVector.dot(normal)).normalize()
        // Right vector (R) = U x N (points right when viewed from outside)
        val rNorm = (uNorm.cross(normal)).normalize()

        var maxRadius = 0.001f
        for (v in vertices) {
            val dist = (v - centroid).length()
            if (dist > maxRadius) maxRadius = dist
        }

        val cell = getCellBounds(faceNumber)
        val uCenter = (cell[0] + cell[2]) * 0.5f
        val vCenter = (cell[1] + cell[3]) * 0.5f

        val cellWidth = cell[2] - cell[0]
        val cellHeight = cell[3] - cell[1]
        val minCellDim = minOf(cellWidth, cellHeight)
        // Scale to fit face nicely inside cell with padding
        val scale = (minCellDim * 0.44f) / maxRadius

        return vertices.map { v ->
            val rel = v - centroid
            val x2d = rel.dot(rNorm)
            val y2d = rel.dot(uNorm)

            val u = uCenter + x2d * scale
            // v is inverted because OpenGL texture coordinates have v=0 at top in Canvas
            val vCoord = vCenter - y2d * scale
            Pair(u, vCoord)
        }
    }

    fun computeNormal(v1: Vector3, v2: Vector3, v3: Vector3): Vector3 {
        val edge1 = v2 - v1
        val edge2 = v3 - v1
        return edge1.cross(edge2).normalize()
    }

    /**
     * Calculates the rotation quaternion that points the face normal directly towards the camera (+Z),
     * and aligns the face's upVector with the camera's up (+Y) so the number is right-side up.
     */
    fun computeFaceTargetRotation(normal: Vector3, upVector: Vector3): Quaternion {
        // Step 1: Rotate normal to +Z
        val qAlign = Quaternion.rotationBetween(normal, Vector3.UNIT_Z)

        // Step 2: Transform upVector by qAlign
        val transformedUp = qAlign.rotate(upVector)

        // Step 3: Compute angle in XY plane to rotate transformedUp to +Y (0, 1, 0)
        // In the XY plane, angle of (x, y) relative to +Y (0, 1) is atan2(x, y)
        val currentAngle = atan2(transformedUp.x, transformedUp.y)
        // Rotate around Z axis by +currentAngle to align transformedUp with +Y
        val qTwist = Quaternion.fromAxisAngle(Vector3.UNIT_Z, currentAngle)

        return (qTwist * qAlign).normalize()
    }
}
