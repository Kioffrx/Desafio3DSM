package com.udb.aprendeapp.model

import java.io.IOException

class RecursoRepository(private val api: ApiService = RetrofitClient.api) {

    suspend fun obtenerTodos(): List<Recurso> {
        val respuesta = api.obtenerRecursos()
        return when {
            respuesta.isSuccessful -> respuesta.body().orEmpty()
            respuesta.code() == 404 -> emptyList()
            else -> throw IOException("HTTP ${respuesta.code()}")
        }
    }

    suspend fun obtenerPorId(id: String): Recurso {
        val respuesta = api.obtenerRecurso(id)
        if (!respuesta.isSuccessful) throw IOException("HTTP ${respuesta.code()}")
        return respuesta.body() ?: throw IOException("Respuesta vacía")
    }

    suspend fun crear(recurso: Recurso): Recurso {
        val respuesta = api.crearRecurso(recurso)
        if (!respuesta.isSuccessful) throw IOException("HTTP ${respuesta.code()}")
        return respuesta.body() ?: throw IOException("Respuesta vacía")
    }

    suspend fun actualizar(id: String, recurso: Recurso): Recurso {
        val respuesta = api.actualizarRecurso(id, recurso)
        if (!respuesta.isSuccessful) throw IOException("HTTP ${respuesta.code()}")
        return respuesta.body() ?: throw IOException("Respuesta vacía")
    }

    suspend fun eliminar(id: String) {
        val respuesta = api.eliminarRecurso(id)
        if (!respuesta.isSuccessful) throw IOException("HTTP ${respuesta.code()}")
    }
}