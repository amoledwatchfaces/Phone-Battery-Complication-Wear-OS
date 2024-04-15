package com.weartools.phonebattcomp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

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
        val canvasSize = 96

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
                    x = 0F
                    y = 0F
                }
                1 -> {
                    x = 48F
                    y = 0F
                }
                2 -> {
                    x = if (numByteArrays == 3) 24F else 0F
                    y = 48F
                }
                else -> {
                    x = 48F
                    y = 48F
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
                    generatePlusBitmap()
                }
            else {
                Bitmap.createScaledBitmap(
                subBitmap,
                48,
                48,
                false)}

            canvas.drawBitmap(scaledBitmap, x, y, paint)
        }

        return resultBitmap
    }


    fun generatePlusBitmap(): Bitmap {
        val plusSize = 48
        val plusBitmap = Bitmap.createBitmap(plusSize, plusSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(plusBitmap)
        val paint = Paint()

        // Set the plus sign color to white
        paint.color = Color.WHITE
        paint.strokeWidth = plusSize.toFloat() / 8  // Adjust this value for desired line thickness

        // Calculate the position to center the 32x32 plus sign inside the 48x48 bitmap
        val plusCenterX = plusSize.toFloat() / 2
        val plusCenterY = plusSize.toFloat() / 2
        val plusHalfSize = 22 / 2

        // Draw the plus sign in the center of the bitmap
        canvas.drawLine(plusCenterX - plusHalfSize, plusCenterY, plusCenterX + plusHalfSize, plusCenterY, paint)
        canvas.drawLine(plusCenterX, plusCenterY - plusHalfSize, plusCenterX, plusCenterY + plusHalfSize, paint)

        return plusBitmap
    }
}