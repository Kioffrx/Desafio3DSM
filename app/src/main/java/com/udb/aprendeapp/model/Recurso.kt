package com.udb.aprendeapp.model

data class Recurso(
    val id: String? = null,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val enlace: String,
    val imagen: String,
    val sumaCalificaciones: Int = 0,
    val totalVotos: Int = 0
) {
    val promedio: Float
        get() = if (totalVotos == 0) 0f else sumaCalificaciones.toFloat() / totalVotos
}