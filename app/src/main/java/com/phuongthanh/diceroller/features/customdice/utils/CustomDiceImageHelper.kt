package com.phuongthanh.diceroller.features.customdice.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.features.customdice.model.CustomDiceFace
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.min

object CustomDiceImageHelper {

    data class PresetIconItem(val id: String, val resId: Int)

    val PRESET_ICONS = listOf(
        PresetIconItem("ic_crown", R.drawable.ic_crown),
        PresetIconItem("ic_trophy", R.drawable.ic_trophy),
        PresetIconItem("ic_star_rate", R.drawable.ic_star_rate),
        PresetIconItem("ic_nav_dice", R.drawable.ic_nav_dice),
        PresetIconItem("ic_nav_party", R.drawable.ic_nav_party),
        PresetIconItem("ic_nav_roulette", R.drawable.ic_nav_roulette),
        PresetIconItem("ic_theme", R.drawable.ic_theme),
        PresetIconItem("ic_confetti_left", R.drawable.ic_confetti_left),
        PresetIconItem("ic_shake", R.drawable.ic_shake),
        PresetIconItem("ic_vibrate", R.drawable.ic_vibrate),
        PresetIconItem("ic_warning", R.drawable.ic_warning),
        PresetIconItem("ic_refresh", R.drawable.ic_refresh)
    )

    fun getPresetIconRes(iconName: String): Int {
        val cleanName = iconName.removePrefix("icon:")
        return PRESET_ICONS.firstOrNull { it.id == cleanName }?.resId ?: R.drawable.ic_theme
    }

    /**
     * Decodes, rotates per EXIF, center-crops to square based on min(width, height),
     * masks into a circular bitmap, and saves as transparent PNG to internal storage.
     */
    fun saveUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver

            // 1. Determine orientation from EXIF
            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                contentResolver.openInputStream(sourceUri)?.use { input ->
                    val exif = ExifInterface(input)
                    orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            } catch (_: Exception) {
                // Ignore EXIF parsing failures
            }

            // 2. Decode bounds to compute inSampleSize to prevent OutOfMemoryError
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            var inSampleSize = 1
            val maxDimension = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
            if (maxDimension > 1024) {
                while ((maxDimension / inSampleSize) > 1024) {
                    inSampleSize *= 2
                }
            }

            // 3. Decode actual bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val rawBitmap = contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return saveUriToInternalStorageRaw(context, sourceUri)

            // 4. Handle EXIF rotation
            val orientedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(rawBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(rawBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(rawBitmap, 270f)
                else -> rawBitmap
            }
            if (orientedBitmap != rawBitmap) {
                rawBitmap.recycle()
            }

            // 5. Fit to smaller dimension: center-crop square with min(width, height)
            val width = orientedBitmap.width
            val height = orientedBitmap.height
            val minDim = min(width, height)
            val cropX = (width - minDim) / 2
            val cropY = (height - minDim) / 2

            val squareBitmap = Bitmap.createBitmap(orientedBitmap, cropX, cropY, minDim, minDim)
            if (squareBitmap != orientedBitmap) {
                orientedBitmap.recycle()
            }

            // 6. Scale to standard icon size (cap at 512px)
            val targetSize = min(minDim, 512)
            val scaledBitmap = if (squareBitmap.width != targetSize) {
                val scaled = Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
                if (scaled != squareBitmap) {
                    squareBitmap.recycle()
                }
                scaled
            } else {
                squareBitmap
            }

            // 7. Mask into a clean circle with anti-aliasing
            val circularBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(circularBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            val radius = targetSize / 2f
            canvas.drawCircle(radius, radius, radius, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

            if (scaledBitmap != circularBitmap) {
                scaledBitmap.recycle()
            }

            // 8. Save circular PNG with transparency to internal storage
            val imagesDir = File(context.filesDir, "custom_dice_images").apply { mkdirs() }
            val targetFile = File(imagesDir, "face_${UUID.randomUUID()}.png")
            FileOutputStream(targetFile).use { output ->
                circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            circularBitmap.recycle()

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            saveUriToInternalStorageRaw(context, sourceUri)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun saveUriToInternalStorageRaw(context: Context, sourceUri: Uri): String? {
        return try {
            val imagesDir = File(context.filesDir, "custom_dice_images").apply { mkdirs() }
            val targetFile = File(imagesDir, "face_${UUID.randomUUID()}.png")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (_: Exception) {
            sourceUri.toString()
        }
    }

    fun loadFaceImage(imageView: ImageView, iconOrPath: String) {
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        if (iconOrPath.startsWith("icon:")) {
            val resId = getPresetIconRes(iconOrPath)
            imageView.setImageResource(resId)
            imageView.imageTintList = ColorStateList.valueOf(Color.parseColor("#1D2939"))
        } else if (iconOrPath.isNotBlank()) {
            try {
                val file = File(iconOrPath)
                if (file.exists()) {
                    imageView.setImageURI(null)
                    imageView.setImageURI(Uri.fromFile(file))
                    imageView.imageTintList = null
                } else {
                    imageView.setImageURI(null)
                    imageView.setImageURI(Uri.parse(iconOrPath))
                    imageView.imageTintList = null
                }
            } catch (_: Exception) {
                imageView.setImageResource(R.drawable.ic_theme)
                imageView.imageTintList = ColorStateList.valueOf(Color.parseColor("#1D2939"))
            }
        } else {
            imageView.setImageResource(R.drawable.ic_theme)
            imageView.imageTintList = ColorStateList.valueOf(Color.parseColor("#1D2939"))
        }
    }

    fun renderFace(
        face: CustomDiceFace,
        imageView: ImageView,
        textView: TextView,
        isPlaceholder: Boolean = false
    ) {
        if (isPlaceholder) {
            imageView.visibility = View.VISIBLE
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.layoutParams.width = (48 * imageView.resources.displayMetrics.density).toInt()
            imageView.layoutParams.height = (48 * imageView.resources.displayMetrics.density).toInt()
            imageView.setImageResource(R.drawable.ic_add)
            imageView.imageTintList = ColorStateList.valueOf(Color.parseColor("#475467"))

            textView.visibility = View.VISIBLE
            textView.text = textView.context.getString(R.string.tap_to_customize_face)
            textView.textSize = 14f
            textView.setTextColor(Color.parseColor("#667085"))
            textView.setTypeface(null, Typeface.NORMAL)
            return
        }

        when (face.formatType) {
            "emoji" -> {
                imageView.visibility = View.GONE
                textView.visibility = View.VISIBLE
                textView.text = face.iconOrEmoji.ifEmpty { face.text }
                textView.textSize = 52f
                textView.setTypeface(null, Typeface.NORMAL)
            }
            "image" -> {
                imageView.visibility = View.VISIBLE
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                imageView.layoutParams.width = (88 * imageView.resources.displayMetrics.density).toInt()
                imageView.layoutParams.height = (88 * imageView.resources.displayMetrics.density).toInt()
                loadFaceImage(imageView, face.iconOrEmoji)

                textView.visibility = View.GONE
            }
            "image_text" -> {
                imageView.visibility = View.VISIBLE
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                imageView.layoutParams.width = (56 * imageView.resources.displayMetrics.density).toInt()
                imageView.layoutParams.height = (56 * imageView.resources.displayMetrics.density).toInt()
                loadFaceImage(imageView, face.iconOrEmoji)

                textView.visibility = View.VISIBLE
                textView.text = face.text
                textView.textSize = 16f
                textView.setTextColor(Color.parseColor("#101828"))
                textView.setTypeface(null, Typeface.BOLD)
            }
            else -> { // "text"
                imageView.visibility = View.GONE
                textView.visibility = View.VISIBLE
                textView.text = face.text
                textView.textSize = 20f
                textView.setTextColor(Color.parseColor("#101828"))
                textView.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}
