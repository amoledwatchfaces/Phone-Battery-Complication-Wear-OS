package com.weartools.phonebattcomp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.weartools.phonebattcomp.R

object BitmapCreator {

    // SMALL_IMAGE
    fun createSingleBitmap(byteArray: ByteArray): Bitmap {
        val canvasSize = 96

        val resultBitmap = createBitmap(canvasSize, canvasSize)

        val canvas = Canvas(resultBitmap)

        val subBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val scaledBitmap = subBitmap.scale(72, 72, false)

        canvas.drawBitmap(scaledBitmap, 12F, 12F, null)

        return resultBitmap
    }
    fun createCompositeBitmap(byteArrays: List<ByteArray>, context: Context): Bitmap {
        val canvasSize = 100

        val arraySize = byteArrays.size
        val numByteArrays = arraySize.coerceAtMost(4)

        val resultBitmap = createBitmap(canvasSize, canvasSize)

        val canvas = Canvas(resultBitmap)

        for (i in 0 until numByteArrays) {

            val x: Float
            var y: Float

            when (i) {
                0 -> {
                    x = 1F
                    y = 1F
                }
                1 -> {
                    x = 51F
                    y = 1F
                }
                2 -> {
                    x = if (numByteArrays == 3) 26F else 1F
                    y = 51F
                }
                else -> {
                    x = 51F
                    y = 51F
                }
            }

            if (numByteArrays == 2) {
                y += (canvasSize / 4)
            }

            val byteArray = byteArrays[i]
            val subBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            //Log.w(TAG, "i: $i , arraySize: $arraySize")
            val scaledBitmap =
                if (i == 3 && arraySize >= 5) {
                    generateMoreBitmap(context)
                }
                else {
                    subBitmap.scale(48, 48, false)
                }

            canvas.drawBitmap(scaledBitmap, x, y, null)
        }

        return resultBitmap
    }

    // LONG_TEXT
    fun createLineCompositeBitmap(byteArrays: List<ByteArray>, context: Context): Bitmap {
        val resultBitmap = createBitmap(400, 50)

        val arraySize = byteArrays.size
        val numByteArrays = arraySize.coerceAtMost(8)

        val canvas = Canvas(resultBitmap)

        for (i in 0 until numByteArrays) {
            val totalIconsWidth = numByteArrays * 50
            val startX = (400 - totalIconsWidth) / 2
            val x: Float = startX + i * 50F

            val byteArray = byteArrays[i] // list size item starts from 1 so we need to subtract 1 to get the correct index
            val subBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            //Log.w(TAG, "i: $i , arraySize: $arraySize")
            val scaledBitmap =
                if (i == 7 && arraySize >= 9) {
                    generateMoreBitmap(context)
                }
                else {
                    subBitmap.scale(48, 48, false)
                }
            canvas.drawBitmap(scaledBitmap, x+1F, 0+1F, null)
        }
        return resultBitmap
    }
    fun createLineBitmapEmpty(): Bitmap {
        val resultBitmap = createBitmap(400, 50)

        val canvas = Canvas(resultBitmap)
        val emptyBitmap = createBitmap(48, 48)
        val emptyCanvas = Canvas(emptyBitmap)

        val centerX = 24F
        val centerY = 24F

        val paintText = Paint().apply {
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }
        val textY = centerY + 1 - (paintText.descent() + paintText.ascent()) / 2
        emptyCanvas.drawText("--", centerX, textY, paintText)

        canvas.drawBitmap(emptyBitmap, 175 + 1F, 0F, null)
        return resultBitmap
    }

    // SHORT_TEXT
    fun createLineCompositeBitmapMax4(byteArrays: List<ByteArray>, context: Context): Bitmap {
        val resultBitmap = createBitmap(200, 50)

        val arraySize = byteArrays.size
        val numByteArrays = arraySize.coerceAtMost(4)

        val canvas = Canvas(resultBitmap)

        for (i in 0 until numByteArrays) {
            val totalIconsWidth = numByteArrays * 50
            val startX = (200 - totalIconsWidth) / 2
            val x: Float = startX + i * 50F

            val byteArray = byteArrays[i] // list size item starts from 1 so we need to subtract 1 to get the correct index
            val subBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            //Log.w(TAG, "i: $i , arraySize: $arraySize")
            val scaledBitmap =
                if (i == 3 && arraySize >= 5) {
                    generateMoreBitmap(context)
                }
                else {
                    subBitmap.scale(48, 48, false)
                }
            canvas.drawBitmap(scaledBitmap, x+1F, 0+1F, null)
        }
        return resultBitmap
    }
    fun createLineBitmapEmptyMax4(): Bitmap {
        val resultBitmap = createBitmap(200, 50)

        val canvas = Canvas(resultBitmap)
        val emptyBitmap = createBitmap(48, 48)
        val emptyCanvas = Canvas(emptyBitmap)

        val centerX = 24F
        val centerY = 24F

        val paintText = Paint().apply {
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }
        val textY = centerY + 1 - (paintText.descent() + paintText.ascent()) / 2
        emptyCanvas.drawText("--", centerX, textY, paintText)

        canvas.drawBitmap(emptyBitmap, 75 + 1F, 0F, null)
        return resultBitmap
    }

    fun generateMoreBitmap(context: Context): Bitmap {
        // Load the drawable resource
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_more) ?: throw IllegalArgumentException("Drawable not found")

        // Define the size for the bitmap
        val size = 48

        // Create a bitmap with the specified size
        val plusBitmap = createBitmap(size, size)

        // Create a canvas to draw the drawable onto the bitmap
        val canvas = Canvas(plusBitmap)

        // Set the bounds for the drawable to match the bitmap dimensions
        drawable.setBounds(0, 0, size, size)

        // Draw the drawable onto the canvas
        drawable.draw(canvas)

        return plusBitmap
    }
}