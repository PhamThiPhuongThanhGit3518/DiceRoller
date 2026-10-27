package com.phuongthanh.diceroller.features.customdice.model

data class CustomDiceFace(
    val faceIndex: Int,
    val text: String,
    val colorHex: String = "#344054",
    val formatType: String = "text", // "image", "text", "image_text", "emoji"
    val iconOrEmoji: String = ""
)

data class CustomDice(
    val id: String,
    val title: String,
    val diceCount: Int = 1,
    val faceCount: Int = 6,
    val faces: List<CustomDiceFace>,
    val isPreset: Boolean = false
)

object CustomDicePresetsBank {
    val presets = listOf(
        CustomDice(
            id = "preset_chores",
            title = "House Chores / Việc Nhà",
            diceCount = 1,
            faceCount = 6,
            isPreset = true,
            faces = listOf(
                CustomDiceFace(1, "Rửa bát", "#EF4444", "text"),
                CustomDiceFace(2, "Nấu cơm", "#F59E0B", "text"),
                CustomDiceFace(3, "Quét nhà", "#3B82F6", "text"),
                CustomDiceFace(4, "Đổ rác", "#8B5CF6", "text"),
                CustomDiceFace(5, "Nghỉ ngơi", "#10B981", "text"),
                CustomDiceFace(6, "Làm tất cả", "#EC4899", "text")
            )
        ),
        CustomDice(
            id = "preset_food",
            title = "Dining / Ăn Uống",
            diceCount = 1,
            faceCount = 6,
            isPreset = true,
            faces = listOf(
                CustomDiceFace(1, "Phở", "#EF4444", "text"),
                CustomDiceFace(2, "Cơm tấm", "#F59E0B", "text"),
                CustomDiceFace(3, "Trà sữa", "#3B82F6", "text"),
                CustomDiceFace(4, "Lẩu", "#8B5CF6", "text"),
                CustomDiceFace(5, "Tự nấu", "#10B981", "text"),
                CustomDiceFace(6, "Nhịn ăn", "#64748B", "text")
            )
        ),
        CustomDice(
            id = "preset_bill",
            title = "Bill Split / Trả Tiền",
            diceCount = 1,
            faceCount = 6,
            isPreset = true,
            faces = listOf(
                CustomDiceFace(1, "Tôi bao", "#EF4444", "text"),
                CustomDiceFace(2, "Campuchia", "#3B82F6", "text"),
                CustomDiceFace(3, "Bên phải trả", "#F59E0B", "text"),
                CustomDiceFace(4, "Đối diện trả", "#8B5CF6", "text"),
                CustomDiceFace(5, "Bên trái trả", "#EC4899", "text"),
                CustomDiceFace(6, "Quán bao", "#10B981", "text")
            )
        )
    )
}
