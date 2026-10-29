package com.udb.aprendeapp.model

enum class Rol(val valor: String) {
    ESTUDIANTE("estudiante"),
    DOCENTE("docente");

    companion object {
        fun desde(valor: String?): Rol =
            entries.firstOrNull { it.valor == valor } ?: ESTUDIANTE
    }
}