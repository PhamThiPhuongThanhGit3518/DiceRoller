package com.phuongthanh.diceroller.core.model

import com.phuongthanh.diceroller.features.freeroll.model.DiceType
import com.phuongthanh.diceroller.features.freeroll.viewmodel.FreeRollIntent
import com.phuongthanh.diceroller.features.freeroll.viewmodel.FreeRollUiState
import com.phuongthanh.diceroller.features.freeroll.viewmodel.FreeRollViewModel
import com.phuongthanh.diceroller.features.classic.viewmodel.ClassicPlayerIntent
import com.phuongthanh.diceroller.features.classic.viewmodel.ClassicPlayerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceFaceStyleTest {

    @Test
    fun testDiceFaceStyleEnum() {
        assertEquals(DiceFaceStyle.DOTS, DiceFaceStyle.fromCode("dots"))
        assertEquals(DiceFaceStyle.NUMBERS, DiceFaceStyle.fromCode("numbers"))
        assertEquals(DiceFaceStyle.DOTS, DiceFaceStyle.fromCode("unknown"))
        assertEquals(DiceFaceStyle.DOTS, DiceFaceStyle.fromCode(null))
    }

    @Test
    fun testFreeRollUiStateCanChooseFaceStyle() {
        // D6 should allow face style selection
        val d6State = FreeRollUiState(diceType = DiceType.D6)
        assertTrue(d6State.canChooseFaceStyle)

        // D4 should allow face style selection
        val d4State = FreeRollUiState(diceType = DiceType.D4)
        assertTrue(d4State.canChooseFaceStyle)

        // D8 should NOT allow face style selection
        val d8State = FreeRollUiState(diceType = DiceType.D8)
        assertFalse(d8State.canChooseFaceStyle)

        // D20 should NOT allow face style selection
        val d20State = FreeRollUiState(diceType = DiceType.D20)
        assertFalse(d20State.canChooseFaceStyle)

        // Custom <= 6 should allow face style selection
        val customSmallState = FreeRollUiState(diceType = DiceType.CUSTOM, customMin = 1, customMax = 6)
        assertTrue(customSmallState.canChooseFaceStyle)

        // Custom > 6 should NOT allow face style selection
        val customLargeState = FreeRollUiState(diceType = DiceType.CUSTOM, customMin = 1, customMax = 20)
        assertFalse(customLargeState.canChooseFaceStyle)
    }

    @Test
    fun testFreeRollViewModelSetFaceStyle() {
        val viewModel = FreeRollViewModel()
        assertEquals(DiceFaceStyle.DOTS, viewModel.uiState.value.faceStyle)

        viewModel.processIntent(FreeRollIntent.SetFaceStyle(DiceFaceStyle.NUMBERS))
        assertEquals(DiceFaceStyle.NUMBERS, viewModel.uiState.value.faceStyle)
        assertTrue(viewModel.uiState.value.diceList.all { it.faceStyle == DiceFaceStyle.NUMBERS })

        viewModel.processIntent(FreeRollIntent.SetFaceStyle(DiceFaceStyle.DOTS))
        assertEquals(DiceFaceStyle.DOTS, viewModel.uiState.value.faceStyle)
        assertTrue(viewModel.uiState.value.diceList.all { it.faceStyle == DiceFaceStyle.DOTS })
    }

    @Test
    fun testClassicPlayerViewModelSetFaceStyle() {
        val viewModel = ClassicPlayerViewModel()
        assertEquals(DiceFaceStyle.DOTS, viewModel.uiState.value.faceStyle)

        viewModel.processIntent(ClassicPlayerIntent.SetFaceStyle(DiceFaceStyle.NUMBERS))
        assertEquals(DiceFaceStyle.NUMBERS, viewModel.uiState.value.faceStyle)

        viewModel.processIntent(ClassicPlayerIntent.SetFaceStyle(DiceFaceStyle.DOTS))
        assertEquals(DiceFaceStyle.DOTS, viewModel.uiState.value.faceStyle)
    }

    @Test
    fun testClassicPlayerViewModelDiceCountClampedTo3() {
        val viewModel = ClassicPlayerViewModel()
        assertEquals(1, viewModel.uiState.value.diceCountPerPlayer)

        viewModel.processIntent(ClassicPlayerIntent.ChangeDiceCount(2))
        assertEquals(2, viewModel.uiState.value.diceCountPerPlayer)

        viewModel.processIntent(ClassicPlayerIntent.ChangeDiceCount(3))
        assertEquals(3, viewModel.uiState.value.diceCountPerPlayer)

        // Count > 3 must be clamped to 3 (User requirement: max 3 dice per player)
        viewModel.processIntent(ClassicPlayerIntent.ChangeDiceCount(4))
        assertEquals(3, viewModel.uiState.value.diceCountPerPlayer)

        viewModel.processIntent(ClassicPlayerIntent.ChangeDiceCount(6))
        assertEquals(3, viewModel.uiState.value.diceCountPerPlayer)

        viewModel.processIntent(ClassicPlayerIntent.ChangeDiceCount(0))
        assertEquals(1, viewModel.uiState.value.diceCountPerPlayer)
    }

    @Test
    fun testClassicPlayerViewModelSyncConfiguration() {
        val viewModel = ClassicPlayerViewModel()
        viewModel.processIntent(
            ClassicPlayerIntent.SyncConfiguration(
                diceType = DiceType.D20,
                customMin = 1,
                customMax = 100,
                faceStyle = DiceFaceStyle.NUMBERS
            )
        )
        assertEquals(DiceType.D20, viewModel.uiState.value.diceType)
        assertEquals(DiceFaceStyle.NUMBERS, viewModel.uiState.value.faceStyle)
        assertTrue(viewModel.uiState.value.players.all { it.diceValues.size == 1 })
    }
}
