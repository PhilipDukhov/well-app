package com.well.androidApp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.View

//private fun updateImage(imageUri: Uri) {
//    val maxSize = imageView.size
//
//    GlobalScope.launch {
//        var bitmap: Bitmap
//        println(
//            "spent ${
//                measureTimeMillis {
//                    bitmap = imageUri.getBitmap()
//                }.toDouble() / 1000
//            }"
//        )
//        println(
//            "spent2 ${
//                measureTimeMillis {
//                    bitmap = bitmap.resize(maxSize)
//                }.toDouble() / 1000
//            }"
//        )
//        MainScope().launch {
//            imageView.setImageBitmap(bitmap)
//        }
//    }
//}

private fun Uri.getBitmap(context: Context): Bitmap =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                context.contentResolver,
                this@getBitmap
            )
        )
    } else {
        MediaStore.Images.Media.getBitmap(context.contentResolver, this@getBitmap)
    }

private fun Bitmap.resize(maxSize: Size): Bitmap =
    size.maxContaining(maxSize).let {
        if (size == it) return this
        return Bitmap.createScaledBitmap(this, it.width, it.height, true)
    }

private val Size.aspectRatio: Float
    get() = width.toFloat() / height

private val Bitmap.size: Size
    get() = Size(width, height)

private val View.size: Size
    get() = Size(width, height)

private fun Size.maxContaining(maxSize: Size): Size {
    val newWidth: Int
    val newHeight: Int
    if (aspectRatio < maxSize.aspectRatio) {
        if (width > maxSize.width) {
            newWidth = maxSize.width
            newHeight = (newWidth.toFloat() / aspectRatio).toInt()
        } else return this
    } else {
        if (height > maxSize.height) {
            newHeight = maxSize.height
            newWidth = (newHeight.toFloat() / aspectRatio).toInt()
        } else return this
    }
    return Size(newWidth, newHeight)
}