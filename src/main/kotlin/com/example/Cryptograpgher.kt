package com.example

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun encrypt(bytes: ByteArray, key: ByteArray, iv: ByteArray, alg: String): ByteArray {
    val encryptCipher = Cipher.getInstance(alg).apply {
        init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, alg.split("/").first()), IvParameterSpec(iv))
    }
    return encryptCipher.doFinal(bytes)
}

fun decrypt(bytes: ByteArray, key: ByteArray, iv: ByteArray, alg: String): ByteArray {
    val cipher = Cipher.getInstance(alg).apply {
        init(Cipher.DECRYPT_MODE, SecretKeySpec(key, alg.split("/").first()), IvParameterSpec(iv))
    }
    return cipher.doFinal(bytes)
}