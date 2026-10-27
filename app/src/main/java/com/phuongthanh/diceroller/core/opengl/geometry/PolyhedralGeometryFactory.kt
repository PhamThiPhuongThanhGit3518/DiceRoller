package com.phuongthanh.diceroller.core.opengl.geometry

import com.phuongthanh.diceroller.core.opengl.math.Quaternion
import com.phuongthanh.diceroller.core.opengl.math.Vector3
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import java.util.EnumMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PolyhedralGeometryFactory {

    /**
     * Tỷ lệ bo viền cạnh (Fillet margin):
     * - 0.02f đến 0.03f: Viền mảnh, cạnh và góc sắc nét.
     * - 0.055f (mặc định): Bo tròn tự nhiên như xúc xắc nhựa/resin chuẩn.
     * - 0.08f đến 0.10f: Bo tròn đậm, xúc xắc mềm mại dạng candy/pastel.
     */
    const val DEFAULT_BEVEL_RATIO = 0.08f

    /**
     * Độ mềm của đỉnh chóp (Corner Softness):
     * - 0.0f: Đỉnh giữ nguyên vị trí nhọn ban đầu, vòm cầu nối từ đỉnh đó xuống.
     * - 0.15f (mặc định): Đỉnh được làm tù nhẹ, tạo cảm giác bo tròn mượt mà.
     * - 0.25f: Đỉnh tù nhiều hơn, dạng đầu tròn bầu.
     */
    const val DEFAULT_CORNER_SOFTNESS = 0.15f

    data class DiceBevelConfig(
        val bevelRatio: Float = DEFAULT_BEVEL_RATIO,
        val cornerSoftness: Float = DEFAULT_CORNER_SOFTNESS
    )

    /**
     * BẢNG THIẾT LẬP BO GÓC & VIỀN CẠNH RIÊNG CHO TỪNG LOẠI XÚC XẮC:
     * - bevelRatio: Tỷ lệ bo viền và đỉnh góc so với tâm mặt.
     *   Quy đổi sang % chiều rộng toàn phần của xúc xắc: % Chiều Rộng Bo = (bevelRatio / 2) * 100%
     *   + 0.10f: Bo 5% chiều rộng (viền mảnh sắc nét).
     *   + 0.25f: Bo 12.5% chiều rộng (bo tròn mềm mại tự nhiên).
     *   + 0.40f: Bo 20% chiều rộng (bo tròn đậm đà).
     *   + 0.50f: Bo 25% chiều rộng (bo tròn sâu kiểu xúc xắc boardgame/casino cao cấp).
     *   + 0.60f: Bo 30% chiều rộng (bo tròn sâu tối đa 30% chiều rộng xúc xắc).
     * - cornerSoftness: Độ mềm làm tù vòm cầu đỉnh góc (0.0f = vòm cầu chuẩn CAD 100%, 0.15f = làm tù nhẹ).
     *
     * Bạn có thể tùy chỉnh trực tiếp thông số của bất kỳ loại xúc xắc nào tại đây:
     */
    val BEVEL_CONFIGS = mapOf(
        DiceType.D4 to DiceBevelConfig(bevelRatio = 0.25f, cornerSoftness = 0.0f),
        DiceType.D6 to DiceBevelConfig(bevelRatio = 0.35f, cornerSoftness = 0.0f),
        DiceType.D8 to DiceBevelConfig(bevelRatio = 0.16f, cornerSoftness = 0.0f),
        DiceType.D10 to DiceBevelConfig(bevelRatio = 0.16f, cornerSoftness = 0.0f),
        DiceType.D12 to DiceBevelConfig(bevelRatio = 0.14f, cornerSoftness = 0.0f),
        DiceType.D20 to DiceBevelConfig(bevelRatio = 0.14f, cornerSoftness = 0.0f),
        DiceType.D100 to DiceBevelConfig(bevelRatio = 0.16f, cornerSoftness = 0.0f),
        DiceType.CUSTOM to DiceBevelConfig(bevelRatio = 0.50f, cornerSoftness = 0.0f)
    )

    /**
     * Góc nghiêng hiển thị kết quả sau khi gieo xong cho từng loại xúc xắc (Resting Tilt Angle):
     * - pitchDeg: Góc cúi xuống theo trục X (+ để nhìn thấy mặt trên xúc xắc).
     * - yawDeg: Góc xoay nghiêng theo trục Y (+ để nhìn thấy mặt bên 3D).
     *
     * Hướng dẫn tùy chỉnh:
     * - Để hiển thị CHÍNH DIỆN 100% thẳng góc camera: đặt pitchDeg = 0f, yawDeg = 0f.
     * - Để hiển thị XOAY NGHIÊNG 3D thấy khối và các mặt bên: đặt pitchDeg > 0f, yawDeg > 0f (ví dụ 15f, 20f).
     */
    val RESTING_ANGLES = mapOf(
        DiceType.D4 to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = -15f, yawDeg = 15f),
        DiceType.D6 to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = 15f, yawDeg = -20f),
        DiceType.D8 to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = 0f, yawDeg = 0f),
        DiceType.D10 to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = 10f, yawDeg = 0f),
        DiceType.D12 to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = 0f, yawDeg = 0f),
        DiceType.D20 to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = 0f, yawDeg = 0f),
        DiceType.D100 to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = 12f, yawDeg = 15f),
        DiceType.CUSTOM to BeveledPolyhedronBuilder.RestingAngle(pitchDeg = 15f, yawDeg = 20f)
    )

    private val meshCache = EnumMap<DiceType, PolyhedralMesh>(DiceType::class.java)

    fun getMesh(type: DiceType): PolyhedralMesh {
        return meshCache.getOrPut(type) {
            when (type) {
                DiceType.D4 -> buildD4()
                DiceType.D6 -> buildD6()
                DiceType.D8 -> buildD8()
                DiceType.D10 -> buildD10(DiceType.D10)
                DiceType.D12 -> buildD12()
                DiceType.D20 -> buildD20()
                DiceType.D100 -> buildD10(DiceType.D100)
                DiceType.CUSTOM -> buildD6()
            }
        }
    }

    fun clearCache() {
        meshCache.clear()
    }



    // ==========================================
    // D4 (Tetrahedron - 4 faces)
    // ==========================================
    private fun buildD4(): PolyhedralMesh {
        val s = 1.05f / sqrt(3f)
        val v0 = Vector3(s, s, s)
        val v1 = Vector3(s, -s, -s)
        val v2 = Vector3(-s, s, -s)
        val v3 = Vector3(-s, -s, s)

        val faces = listOf(
            BeveledPolyhedronBuilder.RawFace(1, listOf(v0, v2, v1), v0),
            BeveledPolyhedronBuilder.RawFace(2, listOf(v0, v3, v2), v0),
            BeveledPolyhedronBuilder.RawFace(3, listOf(v0, v1, v3), v0),
            BeveledPolyhedronBuilder.RawFace(4, listOf(v1, v2, v3), v1)
        )
        val config = BEVEL_CONFIGS[DiceType.D4] ?: DiceBevelConfig()
        return BeveledPolyhedronBuilder.build(
            faces,
            bevelRatio = config.bevelRatio,
            cornerSoftness = config.cornerSoftness,
            restingAngle = RESTING_ANGLES[DiceType.D4] ?: BeveledPolyhedronBuilder.RestingAngle()
        )
    }

    // ==========================================
    // D6 (Cube - 6 faces)
    // ==========================================
    private fun buildD6(): PolyhedralMesh {
        val s = 0.72f
        val faces = listOf(
            // Face 1 (+Z) opp 6 (-Z)
            BeveledPolyhedronBuilder.RawFace(1, listOf(Vector3(-s, s, s), Vector3(-s, -s, s), Vector3(s, -s, s), Vector3(s, s, s)), Vector3.UNIT_Y),
            BeveledPolyhedronBuilder.RawFace(6, listOf(Vector3(s, s, -s), Vector3(s, -s, -s), Vector3(-s, -s, -s), Vector3(-s, s, -s)), Vector3.UNIT_Y),
            // Face 2 (+Y) opp 5 (-Y)
            BeveledPolyhedronBuilder.RawFace(2, listOf(Vector3(-s, s, -s), Vector3(-s, s, s), Vector3(s, s, s), Vector3(s, s, -s)), Vector3(0f, 0f, -1f)),
            BeveledPolyhedronBuilder.RawFace(5, listOf(Vector3(-s, -s, s), Vector3(-s, -s, -s), Vector3(s, -s, -s), Vector3(s, -s, s)), Vector3.UNIT_Z),
            // Face 3 (+X) opp 4 (-X)
            BeveledPolyhedronBuilder.RawFace(3, listOf(Vector3(s, s, s), Vector3(s, -s, s), Vector3(s, -s, -s), Vector3(s, s, -s)), Vector3.UNIT_Y),
            BeveledPolyhedronBuilder.RawFace(4, listOf(Vector3(-s, s, -s), Vector3(-s, -s, -s), Vector3(-s, -s, s), Vector3(-s, s, s)), Vector3.UNIT_Y)
        )
        val config = BEVEL_CONFIGS[DiceType.D6] ?: DiceBevelConfig()
        return BeveledPolyhedronBuilder.build(
            faces,
            bevelRatio = config.bevelRatio,
            cornerSoftness = config.cornerSoftness,
            restingAngle = RESTING_ANGLES[DiceType.D6] ?: BeveledPolyhedronBuilder.RestingAngle()
        )
    }

    // ==========================================
    // D8 (Octahedron - 8 faces)
    // ==========================================
    private fun buildD8(): PolyhedralMesh {
        val r = 0.98f
        val top = Vector3(0f, r, 0f)
        val bottom = Vector3(0f, -r, 0f)
        val vXPos = Vector3(r, 0f, 0f)
        val vZPos = Vector3(0f, 0f, r)
        val vXNeg = Vector3(-r, 0f, 0f)
        val vZNeg = Vector3(0f, 0f, -r)

        val faces = listOf(
            BeveledPolyhedronBuilder.RawFace(1, listOf(top, vXPos, vZPos), top),
            BeveledPolyhedronBuilder.RawFace(2, listOf(top, vZPos, vXNeg), top),
            BeveledPolyhedronBuilder.RawFace(3, listOf(top, vXNeg, vZNeg), top),
            BeveledPolyhedronBuilder.RawFace(4, listOf(top, vZNeg, vXPos), top),
            BeveledPolyhedronBuilder.RawFace(8, listOf(bottom, vZPos, vXPos), bottom * -1f),
            BeveledPolyhedronBuilder.RawFace(7, listOf(bottom, vXNeg, vZPos), bottom * -1f),
            BeveledPolyhedronBuilder.RawFace(6, listOf(bottom, vZNeg, vXNeg), bottom * -1f),
            BeveledPolyhedronBuilder.RawFace(5, listOf(bottom, vXPos, vZNeg), bottom * -1f)
        )
        val config = BEVEL_CONFIGS[DiceType.D8] ?: DiceBevelConfig()
        return BeveledPolyhedronBuilder.build(
            faces,
            bevelRatio = config.bevelRatio,
            cornerSoftness = config.cornerSoftness,
            restingAngle = RESTING_ANGLES[DiceType.D8] ?: BeveledPolyhedronBuilder.RestingAngle()
        )
    }

    // ==========================================
    // D10 (Pentagonal Trapezohedron - 10 faces)
    // ==========================================
    private fun buildD10(type: DiceType = DiceType.D10): PolyhedralMesh {
        val r = 0.95f
        val hApex = r * 1.15f
        // Exact mathematical coplanarity height for pentagonal trapezohedron:
        // The 4 vertices of each kite face are strictly coplanar if and only if:
        // h = hApex * (1 - cos(36°)) / (1 + cos(36°)) = hApex * (1 - 2/sqrt(5)) ≈ 0.105572809 * hApex
        val tan18Sq = (1.0 - cos(PI / 5.0)) / (1.0 + cos(PI / 5.0))
        val h = (hApex * tan18Sq).toFloat()

        val topApex = Vector3(0f, hApex, 0f)
        val bottomApex = Vector3(0f, -hApex, 0f)

        val upperRing = Array(5) { i ->
            val angle = i * 2.0 * PI / 5.0
            Vector3((r * cos(angle)).toFloat(), h, (r * sin(angle)).toFloat())
        }
        val lowerRing = Array(5) { i ->
            val angle = (i * 2.0 * PI / 5.0) + (PI / 5.0)
            Vector3((r * cos(angle)).toFloat(), -h, (r * sin(angle)).toFloat())
        }

        val faces = mutableListOf<BeveledPolyhedronBuilder.RawFace>()
        // Lower face numbering ordered so every opposite face pair sums to 11:
        // Upper 1 <-> Lower 10, Upper 3 <-> Lower 8, Upper 5 <-> Lower 6, Upper 7 <-> Lower 4, Upper 9 <-> Lower 2
        val lowerFaceNumbers = intArrayOf(2, 10, 8, 6, 4)

        for (i in 0 until 5) {
            val prevI = (i - 1 + 5) % 5
            val nextI = (i + 1) % 5

            // Upper kite (Odd faces: 1, 3, 5, 7, 9)
            val faceNumUpper = i * 2 + 1
            val topKite = listOf(
                topApex,
                upperRing[prevI],
                lowerRing[prevI],
                upperRing[i]
            )
            val upUpper = (topApex - lowerRing[prevI]).normalize()
            faces.add(BeveledPolyhedronBuilder.RawFace(faceNumUpper, topKite, upUpper))

            // Lower kite (Even faces: 2, 10, 8, 6, 4)
            val faceNumLower = lowerFaceNumbers[i]
            val bottomKite = listOf(
                bottomApex,
                lowerRing[i],
                upperRing[nextI],
                lowerRing[nextI]
            )
            val upLower = (upperRing[nextI] - bottomApex).normalize()
            faces.add(BeveledPolyhedronBuilder.RawFace(faceNumLower, bottomKite, upLower))
        }

        val config = BEVEL_CONFIGS[type] ?: DiceBevelConfig()
        return BeveledPolyhedronBuilder.build(
            faces,
            bevelRatio = config.bevelRatio,
            cornerSoftness = config.cornerSoftness,
            restingAngle = RESTING_ANGLES[type] ?: BeveledPolyhedronBuilder.RestingAngle()
        )
    }

    // ==========================================
    // D20 (Regular Icosahedron - 20 faces)
    // ==========================================
    private fun buildD20(): PolyhedralMesh {
        val phi = ((1.0 + sqrt(5.0)) / 2.0).toFloat()
        val scale = 0.52f

        val rawVerts = arrayOf(
            Vector3(-1f, phi, 0f), Vector3(1f, phi, 0f), Vector3(-1f, -phi, 0f), Vector3(1f, -phi, 0f),
            Vector3(0f, -1f, phi), Vector3(0f, 1f, phi), Vector3(0f, -1f, -phi), Vector3(0f, 1f, -phi),
            Vector3(phi, 0f, -1f), Vector3(phi, 0f, 1f), Vector3(-phi, 0f, -1f), Vector3(-phi, 0f, 1f)
        ).map { it * scale }

        val faceIndices = listOf(
            Triple(0, 11, 5),   // 1
            Triple(0, 5, 1),    // 2
            Triple(0, 1, 7),    // 3
            Triple(0, 7, 10),   // 4
            Triple(0, 10, 11),  // 5
            Triple(1, 5, 9),    // 6
            Triple(5, 11, 4),   // 7
            Triple(11, 10, 2),  // 8
            Triple(10, 7, 6),   // 9
            Triple(7, 1, 8),    // 10
            Triple(3, 9, 4),    // 11 (opp 10: 10 + 11 = 21)
            Triple(3, 4, 2),    // 12 (opp 9: 9 + 12 = 21)
            Triple(3, 2, 6),    // 13 (opp 8: 8 + 13 = 21)
            Triple(3, 6, 8),    // 14 (opp 7: 7 + 14 = 21)
            Triple(3, 8, 9),    // 15 (opp 6: 6 + 15 = 21)
            Triple(4, 9, 5),    // 16 (opp 5: 5 + 16 = 21)
            Triple(2, 4, 11),   // 17 (opp 4: 4 + 17 = 21)
            Triple(6, 2, 10),   // 18 (opp 3: 3 + 18 = 21)
            Triple(8, 6, 7),    // 19 (opp 2: 2 + 19 = 21)
            Triple(9, 8, 1)     // 20 (opp 1: 1 + 20 = 21)
        )

        val faces = faceIndices.mapIndexed { idx, tri ->
            val faceNum = idx + 1
            val a = rawVerts[tri.first]
            val b = rawVerts[tri.second]
            val c = rawVerts[tri.third]
            BeveledPolyhedronBuilder.RawFace(faceNum, listOf(a, b, c), a)
        }

        val config = BEVEL_CONFIGS[DiceType.D20] ?: DiceBevelConfig()
        return BeveledPolyhedronBuilder.build(
            faces,
            bevelRatio = config.bevelRatio,
            cornerSoftness = config.cornerSoftness,
            restingAngle = RESTING_ANGLES[DiceType.D20] ?: BeveledPolyhedronBuilder.RestingAngle()
        )
    }

    // ==========================================
    // D12 (Regular Dodecahedron - 12 faces)
    // ==========================================
    private fun buildD12(): PolyhedralMesh {
        val phi = ((1.0 + sqrt(5.0)) / 2.0).toFloat()
        val invPhi = 1f / phi
        val scale = 0.52f

        val rawVerts = arrayOf(
            // Cube vertices
            Vector3(-1f, -1f, -1f), Vector3(-1f, -1f, 1f), Vector3(-1f, 1f, -1f), Vector3(-1f, 1f, 1f), // 0..3
            Vector3(1f, -1f, -1f), Vector3(1f, -1f, 1f), Vector3(1f, 1f, -1f), Vector3(1f, 1f, 1f),     // 4..7
            // (0, +-phi, +-1/phi)
            Vector3(0f, -phi, -invPhi), Vector3(0f, -phi, invPhi), Vector3(0f, phi, -invPhi), Vector3(0f, phi, invPhi), // 8..11
            // (+-1/phi, 0, +-phi)
            Vector3(-invPhi, 0f, -phi), Vector3(invPhi, 0f, -phi), Vector3(-invPhi, 0f, phi), Vector3(invPhi, 0f, phi), // 12..15
            // (+-phi, +-1/phi, 0)
            Vector3(-phi, -invPhi, 0f), Vector3(-phi, invPhi, 0f), Vector3(phi, -invPhi, 0f), Vector3(phi, invPhi, 0f)  // 16..19
        ).map { it * scale }

        // 12 regular pentagons forming a 100% closed, watertight manifold.
        // Paired opposite faces sum to 13: (1, 12), (2, 11), (3, 10), (4, 9), (5, 8), (6, 7)
        val pentagons = listOf(
            Triple(1, listOf(3, 11, 7, 15, 14), 11),   // Top-front
            Triple(12, listOf(0, 12, 13, 4, 8), 0),    // Bottom-back (opp 1: 1 + 12 = 13)

            Triple(2, listOf(1, 14, 15, 5, 9), 14),    // Front-bottom
            Triple(11, listOf(2, 10, 6, 13, 12), 10),  // Back-top (opp 2: 2 + 11 = 13)

            Triple(3, listOf(7, 19, 18, 5, 15), 7),    // Right-front
            Triple(10, listOf(2, 17, 16, 0, 12), 2),   // Left-back (opp 3: 3 + 10 = 13)

            Triple(4, listOf(6, 13, 4, 18, 19), 6),    // Right-back
            Triple(9, listOf(3, 14, 1, 16, 17), 3),    // Left-front (opp 4: 4 + 9 = 13)

            Triple(5, listOf(6, 19, 7, 11, 10), 10),   // Top-right
            Triple(8, listOf(0, 8, 9, 1, 16), 16),     // Bottom-left (opp 5: 5 + 8 = 13)

            Triple(6, listOf(2, 10, 11, 3, 17), 11),   // Top-left
            Triple(7, listOf(4, 18, 5, 9, 8), 4)       // Bottom-right (opp 6: 6 + 7 = 13)
        )

        val faces = pentagons.map { (faceNum, pentIndices, upIdx) ->
            val verts = pentIndices.map { rawVerts[it] }
            val upRef = rawVerts[upIdx]
            BeveledPolyhedronBuilder.RawFace(faceNum, verts, upRef)
        }

        val config = BEVEL_CONFIGS[DiceType.D12] ?: DiceBevelConfig()
        return BeveledPolyhedronBuilder.build(
            faces,
            bevelRatio = config.bevelRatio,
            cornerSoftness = config.cornerSoftness,
            restingAngle = RESTING_ANGLES[DiceType.D12] ?: BeveledPolyhedronBuilder.RestingAngle()
        )
    }
}

