package com.volpis.gallery_module.presentation.utils

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class RoundedCornersTransformation(
    private val radius: Int,
    private val margin: Int = 0,
) : BitmapTransformation() {

    private val id = RoundedCornersTransformation::class.simpleName ?: "RoundedCornersTransformation"
    private val idBytes = id.toByteArray(CHARSET)

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(idBytes)
        messageDigest.update(byteArrayOf(radius.toByte(), margin.toByte()))
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int,
    ): Bitmap {
        val width = toTransform.width
        val height = toTransform.height

        val bitmap = pool.get(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setHasAlpha(true)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val rect = RectF(
            margin.toFloat(), margin.toFloat(), width - margin.toFloat(), height - margin.toFloat()
        )

        canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), paint)

        return bitmap
    }

    override fun equals(other: Any?): Boolean {
        if (other is RoundedCornersTransformation) {
            return radius == other.radius && margin == other.margin
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode() + radius * 100 + margin * 10
    }
}