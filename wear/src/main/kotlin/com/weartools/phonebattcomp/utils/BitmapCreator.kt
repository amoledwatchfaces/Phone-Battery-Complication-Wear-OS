package com.weartools.phonebattcomp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object BitmapCreator {

    fun createSingleBitmap(byteArray: ByteArray): Bitmap {
        val canvasSize = 96

        val resultBitmap = Bitmap.createBitmap(canvasSize,canvasSize, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(resultBitmap)
        val paint = Paint()

        val subBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val scaledBitmap = Bitmap.createScaledBitmap(subBitmap, 72, 72, false)

        canvas.drawBitmap(scaledBitmap, 12F, 12F, paint)

        return resultBitmap
    }

    fun createCompositeBitmap(byteArrays: List<ByteArray>): Bitmap {
        val canvasSize = 100

        val arraySize = byteArrays.size
        val numByteArrays = arraySize.coerceAtMost(4)

        val resultBitmap = Bitmap.createBitmap(canvasSize,canvasSize, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(resultBitmap)
        val paint = Paint()

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
                    generatePlusBitmap(arraySize-3)
                }
            else {
                Bitmap.createScaledBitmap(
                subBitmap,
                48,
                48,
                false
                )
            }

            canvas.drawBitmap(scaledBitmap, x, y, paint)
        }

        return resultBitmap
    }


    fun generatePlusBitmap(plus: Int): Bitmap {
        val plusSize = 48
        val plusBitmap = Bitmap.createBitmap(plusSize, plusSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(plusBitmap)

        val centerX = 24F
        val centerY = 24F

        // Draw the number in the center
        val paintText = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = if (plus < 10) 38f else 28f
            typeface = Typeface.DEFAULT_BOLD
        }

        val textY = centerY + 1 - (paintText.descent() + paintText.ascent()) / 2
        canvas.drawText("+$plus", centerX, textY, paintText)

        return plusBitmap
    }
}