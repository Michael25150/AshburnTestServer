package com.example

import java.nio.ByteBuffer

fun intToBytes(value: Int): ByteArray {
    val buffer = ByteBuffer.allocate(4)
    buffer.putInt(value)
    return buffer.array()
}

fun bytesToInt(bytes: ByteArray): Int {
    val buffer = ByteBuffer.wrap(bytes)
    return buffer.int
}