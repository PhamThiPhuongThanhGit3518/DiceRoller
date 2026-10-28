package com.phuongthanh.diceroller.core.opengl.math

import com.phuongthanh.diceroller.core.opengl.geometry.BeveledPolyhedronBuilder
import com.phuongthanh.diceroller.core.opengl.geometry.MeshBuilderUtils
import com.phuongthanh.diceroller.core.opengl.geometry.PolyhedralGeometryFactory
import com.phuongthanh.diceroller.core.opengl.geometry.PolyhedralMesh
import com.phuongthanh.diceroller.core.opengl.model.Dice3DState
import com.phuongthanh.diceroller.core.opengl.view.Dice3DRenderer
import com.phuongthanh.diceroller.core.opengl.view.DicePartitionHelper
import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Math3DTest {

    @Test
    fun vector3_normalization_hasUnitLength() {
        val v = Vector3(3f, 4f, 0f)
        val unit = v.normalize()
        assertEquals(1.0f, unit.length(), 0.0001f)
    }

    @Test
    fun vector3_crossProduct_isOrthogonal() {
        val x = Vector3(1f, 0f, 0f)
        val y = Vector3(0f, 1f, 0f)
        val z = x.cross(y)
        assertEquals(0f, z.x, 0.0001f)
        assertEquals(0f, z.y, 0.0001f)
        assertEquals(1f, z.z, 0.0001f)
    }

    @Test
    fun quaternion_rotationBetween_alignsVectors() {
        val from = Vector3(1f, 0f, 0f)
        val to = Vector3(0f, 0f, 1f)
        val q = Quaternion.rotationBetween(from, to)

        val rotated = q.rotate(from)
        assertEquals(to.x, rotated.x, 0.001f)
        assertEquals(to.y, rotated.y, 0.001f)
        assertEquals(to.z, rotated.z, 0.001f)
    }

    @Test
    fun quaternion_slerp_endpointsMatch() {
        val q1 = Quaternion.IDENTITY
        val q2 = Quaternion.fromAxisAngle(Vector3.UNIT_Y, Math.PI.toFloat() * 0.5f)

        val start = Quaternion.slerp(q1, q2, 0f)
        val end = Quaternion.slerp(q1, q2, 1f)

        assertEquals(q1.w, start.w, 0.001f)
        assertEquals(q1.y, start.y, 0.001f)

        assertEquals(q2.w, end.w, 0.001f)
        assertEquals(q2.y, end.y, 0.001f)
    }

    @Test
    fun polyhedralFactory_createsMeshesForAllDiceTypes() {
        val types = listOf(DiceType.D4, DiceType.D6, DiceType.D8, DiceType.D10, DiceType.D12, DiceType.D20)
        for (type in types) {
            val mesh = PolyhedralGeometryFactory.getMesh(type)
            assertNotNull("Mesh for $type must not be null", mesh)
            assertTrue("Vertex count must be > 0 for $type", mesh.vertexCount > 0)
            assertTrue("Face count must match for $type", mesh.faceCount >= type.maxValue)

            for (face in 1..type.maxValue) {
                val rot = mesh.targetRotations[face]
                assertNotNull("Target rotation for $type face $face must exist", rot)
            }

            // Verify every single triangle has positive outward winding (not culled by GL_CCW + GL_BACK)
            val fb = mesh.vertexBuffer.duplicate()
            fb.rewind()
            val totalTriangles = mesh.vertexCount / 3
            val stride = PolyhedralMesh.FLOATS_PER_VERTEX

            for (t in 0 until totalTriangles) {
                val base0 = (t * 3) * stride
                val v0 = Vector3(fb.get(base0), fb.get(base0 + 1), fb.get(base0 + 2))
                val base1 = (t * 3 + 1) * stride
                val v1 = Vector3(fb.get(base1), fb.get(base1 + 1), fb.get(base1 + 2))
                val base2 = (t * 3 + 2) * stride
                val v2 = Vector3(fb.get(base2), fb.get(base2 + 1), fb.get(base2 + 2))

                val edge1 = v1 - v0
                val edge2 = v2 - v0
                val triNormal = edge1.cross(edge2)
                val triCentroid = (v0 + v1 + v2) * (1f / 3f)

                val dot = triNormal.dot(triCentroid)
                assertTrue(
                    "Triangle $t in $type has inverted/culled winding order! dot=$dot (v0=$v0, v1=$v1, v2=$v2)",
                    dot > 0.0001f
                )
            }
        }
    }

    @Test
    fun planarUvMapping_isContainedInCell() {
        val v0 = Vector3(0f, 1f, 0f)
        val v1 = Vector3(-1f, -0.5f, 0f)
        val v2 = Vector3(1f, -0.5f, 0f)
        val normal = Vector3(0f, 0f, 1f)
        val up = Vector3(0f, 1f, 0f)

        val uvs = MeshBuilderUtils.mapPlanarFaceUv(listOf(v0, v1, v2), normal, up, 1)
        val bounds = MeshBuilderUtils.getCellBounds(1)

        for ((u, v) in uvs) {
            assertTrue("u ($u) must be >= cell uMin (${bounds[0]})", u >= bounds[0] - 0.01f)
            assertTrue("u ($u) must be <= cell uMax (${bounds[2]})", u <= bounds[2] + 0.01f)
            assertTrue("v ($v) must be >= cell vMin (${bounds[1]})", v >= bounds[1] - 0.01f)
            assertTrue("v ($v) must be <= cell vMax (${bounds[3]})", v <= bounds[3] + 0.01f)
        }
    }

    @Test
    fun dice3DState_rollSettlesOnTargetWithoutReversal() {
        val state = Dice3DState(id = 1, type = DiceType.D20, value = 1)
        state.startRoll(newValue = 20, durationMs = 800L)
        assertTrue(state.isRolling)

        val dt = 0.016f
        var totalTime = 0f
        while (state.isRolling && totalTime < 2.0f) {
            state.update(dt)
            totalTime += dt
        }

        assertEquals(false, state.isRolling)
        val mesh = PolyhedralGeometryFactory.getMesh(DiceType.D20)
        val expectedTarget = mesh.targetRotations[20]!!

        assertEquals(expectedTarget.w, state.currentRotation.w, 0.02f)
        assertEquals(expectedTarget.x, state.currentRotation.x, 0.02f)
        assertEquals(expectedTarget.y, state.currentRotation.y, 0.02f)
        assertEquals(expectedTarget.z, state.currentRotation.z, 0.02f)
    }

    @Test
    fun dice3DState_negativeRestingAngles_settlesCorrectly() {
        val negativeTilt = BeveledPolyhedronBuilder.RestingAngle(pitchDeg = -15f, yawDeg = -20f)
        val s = 0.72f
        val rawFace = BeveledPolyhedronBuilder.RawFace(
            1,
            listOf(Vector3(-s, s, s), Vector3(-s, -s, s), Vector3(s, -s, s), Vector3(s, s, s)),
            Vector3.UNIT_Y
        )
        val mesh = BeveledPolyhedronBuilder.build(
            listOf(rawFace),
            restingAngle = negativeTilt
        )
        val targetRot = mesh.targetRotations[1]
        assertNotNull(targetRot)

        val state = Dice3DState(id = 2, type = DiceType.D6, value = 1)
        state.startRoll(newValue = 1, durationMs = 500L)

        val dt = 0.016f
        var totalTime = 0f
        while (state.isRolling && totalTime < 2.0f) {
            state.update(dt)
            totalTime += dt
        }

        assertEquals(false, state.isRolling)
        assertTrue(state.currentRotation.w != 0f || state.currentRotation.x != 0f)
    }

    @Test
    fun d10_mesh_hasAllFacesAndTargetRotationsFaceCameraDirectly() {
        val mesh = PolyhedralGeometryFactory.getMesh(DiceType.D10)
        assertEquals(10, mesh.faceCount)

        for (face in 1..10) {
            val rot = mesh.targetRotations[face]
            assertNotNull("Target rotation for D10 face $face must not be null", rot)
        }
    }

    @Test
    fun diceType_isGreaterThanD20_identifiesD100AndCustom() {
        assertEquals(false, DiceType.D4.isGreaterThanD20)
        assertEquals(false, DiceType.D6.isGreaterThanD20)
        assertEquals(false, DiceType.D8.isGreaterThanD20)
        assertEquals(false, DiceType.D10.isGreaterThanD20)
        assertEquals(false, DiceType.D12.isGreaterThanD20)
        assertEquals(false, DiceType.D20.isGreaterThanD20)
        assertEquals(true, DiceType.D100.isGreaterThanD20)
        assertEquals(true, DiceType.CUSTOM.isGreaterThanD20)

        val d6Item = com.phuongthanh.diceroller.features.freeroll.model.DiceItem(0, DiceType.D6, 1)
        val d100Item = com.phuongthanh.diceroller.features.freeroll.model.DiceItem(1, DiceType.D100, 50)
        val customSmall = com.phuongthanh.diceroller.features.freeroll.model.DiceItem(2, DiceType.CUSTOM, 5, customMin = 1, customMax = 15)
        val customLarge = com.phuongthanh.diceroller.features.freeroll.model.DiceItem(3, DiceType.CUSTOM, 50, customMin = 1, customMax = 100)

        assertEquals(false, d6Item.isTileMode)
        assertEquals(true, d100Item.isTileMode)
        assertEquals(true, customSmall.isTileMode)
        assertEquals(true, customLarge.isTileMode)
    }

    @Test
    fun diceRenderer_scaleConstants_areValid() {
        assertTrue(Dice3DRenderer.SCALE_1_DICE in 0.5f..1.5f)
        assertTrue(Dice3DRenderer.SCALE_2_DICE in 0.4f..1.5f)
        assertTrue(Dice3DRenderer.SCALE_3_DICE in 0.4f..1.5f)
        assertTrue(Dice3DRenderer.SCALE_4_DICE in 0.3f..1.5f)
        assertTrue(Dice3DRenderer.SCALE_MANY_DICE in 0.3f..1.5f)
    }

    @Test
    fun diceRenderer_getDiceScale_returnsExpectedScales() {
        val renderer = Dice3DRenderer()
        assertEquals(Dice3DRenderer.SCALE_1_DICE, renderer.getDiceScale(1), 0.001f)
        assertEquals(Dice3DRenderer.SCALE_2_DICE, renderer.getDiceScale(2), 0.001f)
        assertEquals(Dice3DRenderer.SCALE_3_DICE, renderer.getDiceScale(3), 0.001f)
        assertEquals(Dice3DRenderer.SCALE_4_DICE, renderer.getDiceScale(4), 0.001f)
        assertEquals(Dice3DRenderer.SCALE_MANY_DICE, renderer.getDiceScale(5), 0.001f)
        assertEquals(Dice3DRenderer.SCALE_MANY_DICE, renderer.getDiceScale(9), 0.001f)

        // Custom scale overrides
        renderer.customScale2Dice = 0.75f
        assertEquals(0.75f, renderer.getDiceScale(2), 0.001f)
    }

    @Test
    fun dicePartitionHelper_returnsCorrectPartitionCounts() {
        assertEquals(1, DicePartitionHelper.calculatePartitionRects(1, 200f, 200f).size)
        assertEquals(2, DicePartitionHelper.calculatePartitionRects(2, 200f, 200f).size)
        assertEquals(3, DicePartitionHelper.calculatePartitionRects(3, 200f, 200f).size)
        assertEquals(4, DicePartitionHelper.calculatePartitionRects(4, 200f, 200f).size)
        assertEquals(8, DicePartitionHelper.calculatePartitionRects(8, 200f, 200f).size)
        assertEquals(9, DicePartitionHelper.calculatePartitionRects(9, 200f, 200f).size)
    }
}
