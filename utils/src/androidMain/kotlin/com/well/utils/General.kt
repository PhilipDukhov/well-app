package com.well.utils

//import java.io.ByteArrayOutputStream
//import java.io.InputStream
//
//fun InputStream.readBytes(): ByteArray {
//    val byteBuffer = ByteArrayOutputStream()
//    val bufferSize = 1024
//    val buffer = ByteArray(bufferSize)
//    var len: Int
//    while (read(buffer).also { len = it } != -1) {
//        byteBuffer.write(buffer, 0, len)
//    }
//    return byteBuffer.toByteArray()
//}