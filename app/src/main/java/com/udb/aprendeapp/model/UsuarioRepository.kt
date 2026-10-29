package com.udb.aprendeapp.model

import java.io.IOException

class UsuarioRepository(private val api: ApiService = RetrofitClient.api) {

    // MockAPI responde 404 cuando el filtro no encuentra nada: se trata como lista vacía
    suspend fun buscarPorEmail(email: String): List<Usuario> {
        val respuesta = api.buscarUsuariosPorEmail(email)
        return when {
            respuesta.isSuccessful -> respuesta.body().orEmpty()
            respuesta.code() == 404 -> emptyList()
            else -> throw IOException("HTTP ${respuesta.code()}")
        }
    }

    suspend fun crear(usuario: Usuario): Usuario {
        val respuesta = api.crearUsuario(usuario)
        if (!respuesta.isSuccessful) throw IOException("HTTP ${respuesta.code()}")
        return respuesta.body() ?: throw IOException("Respuesta vacía")
    }
}