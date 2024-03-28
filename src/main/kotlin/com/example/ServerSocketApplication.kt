package com.example

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


private val logger = KotlinLogging.logger { }

fun main(args: Array<String>) {
    val port = System.getProperty("port")?.toIntOrNull()

    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("0.0.0.0", (port ?: 9002))
        logger.info { "Server is listening at ${serverSocket.localAddress}" }
        while (true) {
            val socket = serverSocket.accept()
            logger.info { "Accepted ${socket.remoteAddress}" }
            launch {
                try {
                    val receiveChannel = socket.openReadChannel()
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    while (true) {
                        val sizeBuffer = ByteArray(INTEGER_SIZE_BUFFER_LENGTH)
                        receiveChannel.readFully(sizeBuffer, 0, INTEGER_SIZE_BUFFER_LENGTH)
                        val lengthOfPayload = bytesToInt(sizeBuffer)

                        val requestPayload = ByteArray(lengthOfPayload)
                        receiveChannel.readFully(requestPayload, 0, lengthOfPayload)

                        logger.debug { "Read the following bytes: ${requestPayload.joinToString("")}" }

                        val responsePayload = processRequest(requestPayload)
                        val resp = intToBytes(responsePayload.size) + responsePayload

                        sendChannel.writeFully(resp, 0, responsePayload.size)
                    }
                } catch (e: ClosedReceiveChannelException) {
                    logger.info { "Connection was closed from client side" }
                } catch (e: Throwable) {
                    logger.error { "Following error occurred: $e" }
                } finally {
                    socket.close()
                    logger.info { "Closed socket connection" }
                }
            }
        }
    }
}

private const val INTEGER_SIZE_BUFFER_LENGTH = 4

fun processRequest(requestPayload: ByteArray): ByteArray {
    val unpacked = unpack(requestPayload.toMutableList())
    val data = unpacked.data.toByteArray()
    val key = unpacked.key.toByteArray()
    val iv = unpacked.vec.toByteArray()
    val alg = unpacked.algorithmName
    val encode = unpacked.encode

    val cryptoResult = if (unpacked.encode) encrypt(data, key, iv, alg) else decrypt(data, key, iv, alg)

    val response = EncodeData(
        cryptoResult.toMutableList(),
        key.toMutableList(),
        iv.toMutableList(),
        alg,
        encode
    )
    return pack(response).toByteArray()
}