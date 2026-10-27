package com.phuongthanh.diceroller.core.opengl.geometry

import com.phuongthanh.diceroller.core.opengl.math.Quaternion
import com.phuongthanh.diceroller.core.opengl.math.Vector3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-quality CAD fillet geometric builder that produces photorealistic rounded dice.
 * Uses a 2-segment cylindrical edge fillet and convex spherical corner domes,
 * eliminating flat truncated slices and creating smooth, continuous curved contours.
 */
object BeveledPolyhedronBuilder {

    data class RawFace(
        val faceNum: Int,
        val vertices: List<Vector3>,
        val upVector: Vector3
    )

    private data class HalfEdge(
        val faceIdx: Int,
        val fromG: Int,
        val toG: Int,
        val insetFrom: Vector3,
        val insetTo: Vector3,
        val faceNormal: Vector3
    )

    private data class EdgeKey(val u: Int, val v: Int) {
        companion object {
            fun of(a: Int, b: Int): EdgeKey =
                if (a < b) EdgeKey(a, b) else EdgeKey(b, a)
        }
    }

    data class RestingAngle(
        val pitchDeg: Float = 0f,
        val yawDeg: Float = 0f,
        val rollDeg: Float = 0f
    )

    private data class BoundaryPoint(
        val pos: Vector3,
        val normal: Vector3
    )

    /**
     * Builds a photorealistic rounded polyhedral mesh with continuous fillets.
     *
     * @param rawFaces The polygonal faces defining the polyhedron.
     * @param bevelRatio Fillet margin fraction (e.g. 0.055f for 5.5% rounded fillet).
     * @param cornerSoftness Softness of corner apex (0.0f = sharp original tip, 0.2f = softened dome).
     * @param restingAngle Optional resting tilt angle (pitch & yaw) when settled after rolling.
     */
    fun build(
        rawFaces: List<RawFace>,
        bevelRatio: Float = 0.055f,
        cornerSoftness: Float = 0.15f,
        restingAngle: RestingAngle = RestingAngle()
    ): PolyhedralMesh {
        val floatList = mutableListOf<Float>()
        val targetRotations = mutableMapOf<Int, Quaternion>()

        // 1. Identify and weld unique vertices across all faces
        val uniqueVerts = mutableListOf<Vector3>()
        fun getGlobalVertexIndex(v: Vector3): Int {
            for (i in uniqueVerts.indices) {
                val diff = uniqueVerts[i] - v
                if (diff.dot(diff) < 1e-6f) {
                    return i
                }
            }
            uniqueVerts.add(v)
            return uniqueVerts.size - 1
        }

        // 2. Validate face orientation (outward normal & CCW winding) and compute centroids
        class FaceData(
            val raw: RawFace,
            val orderedVerts: List<Vector3>,
            val globalIndices: List<Int>,
            val normal: Vector3,
            val upVector: Vector3,
            val centroid: Vector3,
            val insetVerts: List<Vector3>
        )

        val facesData = mutableListOf<FaceData>()

        for (rf in rawFaces) {
            var cx = 0f; var cy = 0f; var cz = 0f
            for (v in rf.vertices) { cx += v.x; cy += v.y; cz += v.z }
            val centroid = Vector3(cx / rf.vertices.size, cy / rf.vertices.size, cz / rf.vertices.size)

            var normal = MeshBuilderUtils.computeNormal(rf.vertices[0], rf.vertices[1], rf.vertices[2])
            var ordered = rf.vertices
            if (normal.dot(centroid) < 0f) {
                normal = normal * -1f
                ordered = rf.vertices.reversed()
            }

            val upNorm = (rf.upVector - normal * rf.upVector.dot(normal)).normalize()
            val baseRotation = MeshBuilderUtils.computeFaceTargetRotation(normal, upNorm)

            // Apply resting tilt (pitch around X axis, yaw around Y axis, roll around Z axis in camera view space)
            val radPitch = (restingAngle.pitchDeg * Math.PI / 180.0).toFloat()
            val radYaw = (restingAngle.yawDeg * Math.PI / 180.0).toFloat()
            val radRoll = (restingAngle.rollDeg * Math.PI / 180.0).toFloat()
            val qPitch = Quaternion.fromAxisAngle(Vector3.UNIT_X, radPitch)
            val qYaw = Quaternion.fromAxisAngle(Vector3.UNIT_Y, radYaw)
            val qRoll = Quaternion.fromAxisAngle(Vector3.UNIT_Z, radRoll)
            val qTilt = qYaw * qPitch * qRoll

            targetRotations[rf.faceNum] = (qTilt * baseRotation).normalize()

            val globalIndices = ordered.map { getGlobalVertexIndex(it) }

            // Inset vertices towards centroid
            val insetVerts = ordered.map { v ->
                v + (centroid - v) * bevelRatio
            }

            facesData.add(FaceData(rf, ordered, globalIndices, normal, upNorm, centroid, insetVerts))
        }

        // 3. Generate Inset Faces (Flat central face displaying crisp number)
        for (fd in facesData) {
            val uvs = MeshBuilderUtils.mapPlanarFaceUv(fd.insetVerts, fd.normal, fd.upVector, fd.raw.faceNum)
            val k = fd.insetVerts.size

            if (k == 4) {
                val v0 = fd.insetVerts[0]; val uv0 = uvs[0]
                val v1 = fd.insetVerts[1]; val uv1 = uvs[1]
                val v2 = fd.insetVerts[2]; val uv2 = uvs[2]
                val v3 = fd.insetVerts[3]; val uv3 = uvs[3]

                addOrientedTriangle(floatList, v0, fd.normal, uv0.first, uv0.second, v1, fd.normal, uv1.first, uv1.second, v2, fd.normal, uv2.first, uv2.second)
                addOrientedTriangle(floatList, v0, fd.normal, uv0.first, uv0.second, v2, fd.normal, uv2.first, uv2.second, v3, fd.normal, uv3.first, uv3.second)
            } else {
                val v0 = fd.insetVerts[0]; val uv0 = uvs[0]
                for (i in 1 until k - 1) {
                    val v1 = fd.insetVerts[i]; val uv1 = uvs[i]
                    val v2 = fd.insetVerts[i + 1]; val uv2 = uvs[i + 1]

                    addOrientedTriangle(floatList, v0, fd.normal, uv0.first, uv0.second, v1, fd.normal, uv1.first, uv1.second, v2, fd.normal, uv2.first, uv2.second)
                }
            }
        }

        // 4. Edge Fillets (2-segment cylindrical fillet between adjacent faces)
        val solidU = 0.01f
        val solidV = 0.01f

        val edgeMap = mutableMapOf<EdgeKey, MutableList<HalfEdge>>()
        val halfEdgesByFromG = mutableMapOf<Int, MutableList<HalfEdge>>()

        for ((fIdx, fd) in facesData.withIndex()) {
            val k = fd.globalIndices.size
            for (j in 0 until k) {
                val nextJ = (j + 1) % k
                val gFrom = fd.globalIndices[j]
                val gTo = fd.globalIndices[nextJ]
                val pFrom = fd.insetVerts[j]
                val pTo = fd.insetVerts[nextJ]

                val he = HalfEdge(fIdx, gFrom, gTo, pFrom, pTo, fd.normal)
                val key = EdgeKey.of(gFrom, gTo)
                edgeMap.getOrPut(key) { mutableListOf() }.add(he)
                halfEdgesByFromG.getOrPut(gFrom) { mutableListOf() }.add(he)
            }
        }

        // Map to store ridge points along each edge for corner dome stitching
        val edgeRidgeNearVertex = mutableMapOf<Pair<EdgeKey, Int>, Vector3>()
        val edgeNormalMap = mutableMapOf<EdgeKey, Vector3>()

        for ((key, halfEdges) in edgeMap) {
            if (halfEdges.size != 2) continue

            val heA = halfEdges[0]
            val heB = halfEdges[1]

            val vA = uniqueVerts[heA.fromG]
            val vB = uniqueVerts[heA.toG]

            val nA = heA.faceNormal
            val nB = heB.faceNormal
            val nEdge = (nA + nB).normalize()
            edgeNormalMap[key] = nEdge

            val pA_from = heA.insetFrom
            val pA_to = heA.insetTo
            val pB_from = if (heB.fromG == heA.fromG) heB.insetFrom else heB.insetTo
            val pB_to = if (heB.fromG == heA.toG) heB.insetFrom else heB.insetTo

            val chordA = (pA_from + pB_from) * 0.5f
            val chordB = (pA_to + pB_to) * 0.5f

            // Orthogonal projection of chords onto the edge segment (vA -> vB)
            // Ensures edge fillets stay 100% aligned with face insets without tangential shearing
            val edgeVec = vB - vA
            val edgeLen = edgeVec.length()
            val edgeDir = edgeVec.normalize()

            val tA = ((chordA - vA).dot(edgeDir)).coerceIn(0f, edgeLen)
            val tB = ((chordB - vA).dot(edgeDir)).coerceIn(0f, edgeLen)

            val vA_edge = vA + edgeDir * tA
            val vB_edge = vA + edgeDir * tB

            // Exact circular arc curvature factor: sin(alpha/2) / (1 + sin(alpha/2))
            val cosAlpha = (-nA.dot(nB)).coerceIn(-1f, 1f)
            val sinHalf = sqrt(((1f - cosAlpha) * 0.5f).coerceAtLeast(0f))
            val filletCurvature = if (sinHalf > 1e-4f) sinHalf / (1f + sinHalf) else 0.4142f

            val rA = chordA + (vA_edge - chordA) * filletCurvature
            val rB = chordB + (vB_edge - chordB) * filletCurvature

            edgeRidgeNearVertex[key to heA.fromG] = rA
            edgeRidgeNearVertex[key to heA.toG] = rB

            // Fillet Quad 1: Face A side (pA_from -> pA_to -> rB -> rA)
            addOrientedTriangle(floatList, pA_from, nA, solidU, solidV, pA_to, nA, solidU, solidV, rB, nEdge, solidU, solidV)
            addOrientedTriangle(floatList, pA_from, nA, solidU, solidV, rB, nEdge, solidU, solidV, rA, nEdge, solidU, solidV)

            // Fillet Quad 2: Face B side (rA -> rB -> pB_to -> pB_from)
            addOrientedTriangle(floatList, rA, nEdge, solidU, solidV, rB, nEdge, solidU, solidV, pB_to, nB, solidU, solidV)
            addOrientedTriangle(floatList, rA, nEdge, solidU, solidV, pB_to, nB, solidU, solidV, pB_from, nB, solidU, solidV)
        }

        // 5. Convex Spherical Corner Domes (Smooth rounded corners, never sliced flat)
        for (g in uniqueVerts.indices) {
            val vCorner = uniqueVerts[g]
            val nCorner = vCorner.normalize()

            val leaving = halfEdgesByFromG[g] ?: continue
            if (leaving.size < 3) continue

            // Build cyclic ring of alternating face inset points and edge ridge points
            val ringPoints = mutableListOf<BoundaryPoint>()
            var curHE = leaving[0]
            val visitedFaces = mutableSetOf<Int>()

            while (visitedFaces.size < leaving.size) {
                visitedFaces.add(curHE.faceIdx)

                // 1. Face inset point near g
                ringPoints.add(BoundaryPoint(curHE.insetFrom, curHE.faceNormal))

                // 2. Edge ridge point near g
                val edgeKey = EdgeKey.of(curHE.fromG, curHE.toG)
                val ridgePt = edgeRidgeNearVertex[edgeKey to g] ?: vCorner
                val edgeNorm = edgeNormalMap[edgeKey] ?: nCorner
                ringPoints.add(BoundaryPoint(ridgePt, edgeNorm))

                // Find companion half-edge entering g from curHE.toG
                val pair = edgeMap[edgeKey]
                val otherHE = pair?.firstOrNull { it.faceIdx != curHE.faceIdx } ?: break

                // Next half-edge leaving g on otherHE.faceIdx
                val nextHE = halfEdgesByFromG[g]?.firstOrNull { it.faceIdx == otherHE.faceIdx }
                if (nextHE == null || nextHE.faceIdx in visitedFaces) {
                    break
                }
                curHE = nextHE
            }

            if (ringPoints.size < 3) {
                ringPoints.clear()
                for (he in leaving) {
                    ringPoints.add(BoundaryPoint(he.insetFrom, he.faceNormal))
                }
            }

            // 1. Calculate centroid and sphere center for the corner dome
            val numPts = ringPoints.size
            if (numPts < 3) continue

            var cx = 0f; var cy = 0f; var cz = 0f
            var sumNormX = 0f; var sumNormY = 0f; var sumNormZ = 0f
            for (pt in ringPoints) {
                cx += pt.pos.x
                cy += pt.pos.y
                cz += pt.pos.z
                sumNormX += pt.normal.x
                sumNormY += pt.normal.y
                sumNormZ += pt.normal.z
            }
            val cRing = Vector3(cx / numPts, cy / numPts, cz / numPts)
            val meanNorm = Vector3(sumNormX / numPts, sumNormY / numPts, sumNormZ / numPts)
            val meanNormLen = meanNorm.length()
            val dirCorner = if (meanNormLen > 1e-4f) meanNorm.normalize() else nCorner

            var sumDist = 0f
            for (pt in ringPoints) {
                sumDist += (pt.pos - cRing).length()
            }
            val rRing = sumDist / numPts

            // True CAD corner sphere fitting:
            // Tangent to incident faces and edge fillets with sphere center inside the polyhedron
            val sinPhi = sqrt((1f - meanNormLen * meanNormLen).coerceIn(0.01f, 1f))
            val rSphere = rRing / sinPhi
            val hSphere = rSphere * meanNormLen
            val oSphere = cRing - dirCorner * hSphere

            val vApex = oSphere + dirCorner * (rSphere * (1f - cornerSoftness * 0.15f))

            // 2. Multi-tier spherical subdivision via SLERP:
            // Every intermediate ring point lies strictly on the sphere surface of radius rSphere
            val tierFractions = floatArrayOf(0.33f, 0.67f)
            val ringTiers = mutableListOf<List<BoundaryPoint>>()
            ringTiers.add(ringPoints) // Tier 0 is the base ring

            for (t in tierFractions) {
                val tierPts = ringPoints.map { pt ->
                    val dirRing = (pt.pos - oSphere).normalize()
                    val dirT = dirRing.slerp(dirCorner, t)
                    val pos = oSphere + dirT * rSphere
                    BoundaryPoint(pos, dirT)
                }
                ringTiers.add(tierPts)
            }

            // 3. Connect intermediate quad strips between successive tiers
            for (t in 0 until ringTiers.size - 1) {
                val lowerTier = ringTiers[t]
                val upperTier = ringTiers[t + 1]
                for (i in 0 until numPts) {
                    val nextI = (i + 1) % numPts
                    val p0 = lowerTier[i]
                    val p1 = lowerTier[nextI]
                    val q1 = upperTier[nextI]
                    val q0 = upperTier[i]

                    // Quad: p0 -> p1 -> q1 -> q0
                    addOrientedTriangle(
                        floatList,
                        p0.pos, p0.normal, solidU, solidV,
                        p1.pos, p1.normal, solidU, solidV,
                        q1.pos, q1.normal, solidU, solidV
                    )
                    addOrientedTriangle(
                        floatList,
                        p0.pos, p0.normal, solidU, solidV,
                        q1.pos, q1.normal, solidU, solidV,
                        q0.pos, q0.normal, solidU, solidV
                    )
                }
            }

            // 4. Connect highest tier to vApex cap
            val topTier = ringTiers.last()
            for (i in 0 until numPts) {
                val nextI = (i + 1) % numPts
                val p0 = topTier[i]
                val p1 = topTier[nextI]

                addOrientedTriangle(
                    floatList,
                    vApex, nCorner, solidU, solidV,
                    p0.pos, p0.normal, solidU, solidV,
                    p1.pos, p1.normal, solidU, solidV
                )
            }
        }

        val buffer = PolyhedralMesh.createFloatBuffer(floatList.toFloatArray())
        val totalVertices = floatList.size / PolyhedralMesh.FLOATS_PER_VERTEX
        return PolyhedralMesh(buffer, totalVertices, rawFaces.size, targetRotations)
    }

    private fun addOrientedTriangle(
        list: MutableList<Float>,
        v0: Vector3, n0: Vector3, u0: Float, vCoord0: Float,
        v1: Vector3, n1: Vector3, u1: Float, vCoord1: Float,
        v2: Vector3, n2: Vector3, u2: Float, vCoord2: Float
    ) {
        val edge1 = v1 - v0
        val edge2 = v2 - v0
        val normal = edge1.cross(edge2)
        val centroid = (v0 + v1 + v2) * (1f / 3f)

        if (normal.dot(centroid) >= 0f) {
            addVertex(list, v0, n0, u0, vCoord0)
            addVertex(list, v1, n1, u1, vCoord1)
            addVertex(list, v2, n2, u2, vCoord2)
        } else {
            addVertex(list, v0, n0, u0, vCoord0)
            addVertex(list, v2, n2, u2, vCoord2)
            addVertex(list, v1, n1, u1, vCoord1)
        }
    }

    private fun addVertex(
        list: MutableList<Float>,
        pos: Vector3,
        normal: Vector3,
        u: Float,
        v: Float
    ) {
        list.add(pos.x)
        list.add(pos.y)
        list.add(pos.z)
        list.add(normal.x)
        list.add(normal.y)
        list.add(normal.z)
        list.add(u)
        list.add(v)
    }
}
