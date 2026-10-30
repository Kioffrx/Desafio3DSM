package com.udb.aprendeapp.controller

sealed class Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>()
    // mensaje es un id de strings.xml
    data class Error(val mensaje: Int) : Resultado<Nothing>()
}