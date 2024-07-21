package com.weartools.phonebattcomp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object BitmapCreatorLine {

    fun createLineCompositeBitmap(byteArrays: List<ByteArray>): Bitmap {
        val resultBitmap = Bitmap.createBitmap(400,50, Bitmap.Config.ARGB_8888)

        val arraySize = byteArrays.size
        val numByteArrays = arraySize.coerceAtMost(8)

        val canvas = Canvas(resultBitmap)
        val paint = Paint()

        for (i in 0 until numByteArrays) {
            val totalIconsWidth = numByteArrays * 50
            val startX = (400 - totalIconsWidth) / 2
            val x: Float = startX + i * 50F

            val byteArray = byteArrays[i] // list size item starts from 1 so we need to subtract 1 to get the correct index
            val subBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            //Log.w(TAG, "i: $i , arraySize: $arraySize")
            val scaledBitmap =
                if (i == 7 && arraySize >= 9) {
                    generatePlusBitmap(arraySize-7)
                }
            else {
                Bitmap.createScaledBitmap(
                subBitmap,
                48,
                48,
                false)
            }
            canvas.drawBitmap(scaledBitmap, x+1F, 0+1F, paint)
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


    fun createLineCompositeBitmapEmpty(): Bitmap {
        val resultBitmap = Bitmap.createBitmap(400,50, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(resultBitmap)
        val paint = Paint()


        val emptyBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
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

        canvas.drawBitmap(emptyBitmap, 175 + 1F, 0F, paint)
        return resultBitmap
    }
}