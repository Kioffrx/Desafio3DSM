package com.udb.aprendeapp.controller

import com.udb.aprendeapp.R

object PasswordValidator {

    private const val LARGO_MINIMO = 12
    private val ESPECIALES = "!@#$%^&*".toSet()

    // Devuelve los ids de strings de las reglas que NO se cumplen (vacía = válida)
    fun validar(password: String): List<Int> {
        val errores = mutableListOf<Int>()
        if (password.length < LARGO_MINIMO) errores += R.string.password_regla_largo
        if (password.none { it.isUpperCase() }) errores += R.string.password_regla_mayuscula
        if (password.none { it.isLowerCase() }) errores += R.string.password_regla_minuscula
        if (password.none { it.isDigit() }) errores += R.string.password_regla_numero
        if (password.none { it in ESPECIALES }) errores += R.string.password_regla_especial
        return errores
    }
}