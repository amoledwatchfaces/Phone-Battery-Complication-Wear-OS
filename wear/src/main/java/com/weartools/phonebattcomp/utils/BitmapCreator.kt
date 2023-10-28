package com.weartools.phonebattcomp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

object BitmapCreator {

    fun createCompositeBitmap(byteArrays: List<ByteArray>): Bitmap {
        val canvasSize = 96
        val padding = 4

        val numByteArrays = byteArrays.size
        val numRows = if (numByteArrays == 2) 1 else 2
        val numColumns = 2

        val resultBitmap = Bitmap.createBitmap(
            canvasSize * numColumns + (numColumns - 1) * padding,
            canvasSize * numRows + (numRows - 1) * padding,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(resultBitmap)

        val paint = Paint()

        for (i in 0 until numByteArrays) {
            val row = i / numColumns
            val col = i % numColumns
            val x = col * (canvasSize + padding)
            val y = row * (canvasSize + padding)

            if (numByteArrays == 2) {
                // Center the single row vertically
                val yOffset = (resultBitmap.height - canvasSize) / 2
                canvas.translate(0f, yOffset.toFloat())
            }

            val byteArray = byteArrays[i]
            val subBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            val scaledBitmap = Bitmap.createScaledBitmap(subBitmap, canvasSize, canvasSize, false)

            canvas.drawBitmap(scaledBitmap, x.toFloat(), y.toFloat(), paint)
        }

        return resultBitmap
    }
}