package com.example

import java.nio.ByteBuffer

private const val seventhByte = 64
private const val sixthByte = 32
private const val fifthByte = 16
private const val fourthByte = 8
private const val thirdByte = 4
private const val boolTrueTag: Byte = 32.toByte()
private const val boolFalseTag: Byte = 64.toByte()
private const val startEndTag: Byte = 0
private const val bits = 8
private const val bitsForSize = 4
private const val undefinedTypeTag: Byte = 20.toByte()

data class EncodeData(
    val data: MutableList<Byte>,
    val key: MutableList<Byte>,
    val vec: MutableList<Byte>,
    val algorithmName: String,
    val encode: Boolean,
)

class Serializing {

    lateinit var obj: EncodeData
    var bytes: MutableList<Byte> = arrayListOf()

     fun serializeBytesToObject(): EncodeData {
        val startIndex = arrayListOf(1)
        val dataSize = arrayListOf(0)
        val objectData = mutableListOf<Byte>()
        val objectKey = mutableListOf<Byte>()
        val objectVec = mutableListOf<Byte>()
        makingByteArray(startIndex, dataSize, objectData)
        makingByteArray(startIndex, dataSize, objectKey)
        makingByteArray(startIndex, dataSize, objectVec)
        val algorithmName = makingString(startIndex, dataSize[0])
        val encode = translateBoolTag(bytes.size - 2)
        return EncodeData(
            objectData as ArrayList<Byte>,
            objectKey as ArrayList<Byte>,
            objectVec as ArrayList<Byte>,
            algorithmName,
            encode
        )
    }

    fun serializeObjectToBytes(): List<Byte> {
        bytes.add(startEndTag)
        writingByteArray(obj.data)
        writingByteArray(obj.key)
        writingByteArray(obj.vec)
        writingString(obj.algorithmName)
        if (obj.encode) {
            bytes.add(boolTrueTag)
        } else {
            bytes.add(boolFalseTag)
        }
        bytes.add(startEndTag)
        return bytes
    }

    private fun writingStringTag(dataSize: Int) {
        var stringTypeTag = seventhByte + sixthByte
        if (dataSize >= fifthByte) {
            stringTypeTag += fifthByte + thirdByte
            bytes.add(stringTypeTag.toByte())
            countingSizeToBytes(dataSize)
        } else {
            stringTypeTag += dataSize
            bytes.add(stringTypeTag.toByte())
        }
    }

    private fun writingString(name: String) {
        writingStringTag(name.length)
        name.forEach { bytes.add(it.code.toByte()) }
    }

    private fun countingSizeToBytes(dataSize: Int) {
        val data = ByteBuffer.allocate(bitsForSize).putInt(dataSize).array()
        bytes += ByteArray(bitsForSize).toList()
        data.forEachIndexed { index, byte ->
            bytes[bytes.size - bitsForSize + index] = byte
        }
    }

    private fun countingSizeFromBytes(index: Int): Int {
        return (bytes[index - 3].toInt() shl (bits * 3) or
                bytes[index - 2].toInt() shl (bits * 2) or
                bytes[index - 1].toInt() shl bits or
                bytes[index].toInt())
    }

    private fun countingSizeFromTag(size: Byte): Int {
        var dataSize = 0
        var k = fourthByte
        for (i in 0 until bits / 2) {
            if ((size.toInt() and k) != 0) {
                dataSize += k
            }
            k /= 2
        }
        return dataSize
    }

    private fun makingString(startIndex: ArrayList<Int>, dataSize: Int): String {
        var data = dataSize
        val name = StringBuilder()
        if ((bytes[startIndex.first() + data].toInt() and fifthByte) != 0) {
            startIndex[0] = startIndex[0] + data + 5
            data = countingSizeFromBytes(startIndex.first() - 1)
        } else {
            startIndex[0] = startIndex[0] + data + 1
            data = countingSizeFromTag(bytes[startIndex.first() - 1])
        }
        for (i in startIndex.first() until startIndex.first() + data) {
            name.append(bytes[i].toChar())
        }
        return name.toString()
    }

    private fun translateBoolTag(index: Int): Boolean {
        return bytes[index] == boolTrueTag
    }

    private fun makingByteArray(
        startIndex: ArrayList<Int>,
        dataSize: ArrayList<Int>,
        dataBytes: MutableList<Byte>,
    ) {
        startIndex[0] = startIndex[0] + dataSize[0] + bitsForSize + 1
        dataSize[0] = countingSizeFromBytes(startIndex[0] - 1)
        dataBytes.addAll(bytes.subList(startIndex[0], startIndex[0] + dataSize[0]))
    }

    private fun writingByteArray(dataBytes: MutableList<Byte>) {
        bytes.add(undefinedTypeTag)
        val dataSize = dataBytes.size
        countingSizeToBytes(dataSize)
        bytes.addAll(dataBytes)
    }
}

fun pack(objectData: EncodeData): List<Byte> {
    val helper = Serializing()
    helper.obj = objectData
    return helper.serializeObjectToBytes()
}

fun unpack(bytes: MutableList<Byte>): EncodeData {
    val helper = Serializing()
    helper.bytes = bytes
    return helper.serializeBytesToObject()
}
