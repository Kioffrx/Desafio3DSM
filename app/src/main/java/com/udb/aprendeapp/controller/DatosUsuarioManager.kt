package com.udb.aprendeapp.controller

import android.content.Context

// Persistencia local por usuario: favoritos y la calificación que dio a cada recurso.
class DatosUsuarioManager(contexto: Context, private val sesion: SessionManager) {

    private val prefs = contexto.getSharedPreferences("datos_usuario_aprendeapp", Context.MODE_PRIVATE)

    private fun claveFavoritos() = "fav_${sesion.idUsuario()}"
    private fun claveCalificacion(idRecurso: String) = "rate_${sesion.idUsuario()}_$idRecurso"

    fun favoritos(): Set<String> = prefs.getStringSet(claveFavoritos(), emptySet()) ?: emptySet()

    fun esFavorito(idRecurso: String?): Boolean =
        idRecurso != null && favoritos().contains(idRecurso)

    // Devuelve true si quedó como favorito, false si se quitó
    fun alternarFavorito(idRecurso: String): Boolean {
        val nuevos = favoritos().toMutableSet()
        val agregado = nuevos.add(idRecurso)
        if (!agregado) nuevos.remove(idRecurso)
        prefs.edit().putStringSet(claveFavoritos(), nuevos).apply()
        return agregado
    }

    fun calificacionDe(idRecurso: String?): Int =
        if (idRecurso == null) 0 else prefs.getInt(claveCalificacion(idRecurso), 0)

    fun guardarCalificacion(idRecurso: String, estrellas: Int) {
        prefs.edit().putInt(claveCalificacion(idRecurso), estrellas).apply()
    }
}