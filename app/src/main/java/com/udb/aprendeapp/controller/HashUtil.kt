package com.udb.aprendeapp.controller

import java.security.MessageDigest

object HashUtil {
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}