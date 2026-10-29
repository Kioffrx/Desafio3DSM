package com.udb.aprendeapp.controller

import android.content.Context
import com.udb.aprendeapp.model.Rol
import com.udb.aprendeapp.model.Usuario

class SessionManager(contexto: Context) {

    private val prefs = contexto.getSharedPreferences("sesion_aprendeapp", Context.MODE_PRIVATE)

    fun guardar(usuario: Usuario) {
        prefs.edit()
            .putString(KEY_ID, usuario.id)
            .putString(KEY_NOMBRE, usuario.nombre)
            .putString(KEY_ROL, usuario.rol)
            .apply()
    }

    fun haySesion(): Boolean = prefs.getString(KEY_ID, null) != null
    fun idUsuario(): String? = prefs.getString(KEY_ID, null)
    fun nombre(): String = prefs.getString(KEY_NOMBRE, "").orEmpty()
    fun rol(): Rol = Rol.desde(prefs.getString(KEY_ROL, null))
    fun esDocente(): Boolean = rol() == Rol.DOCENTE

    fun cerrar() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ID = "id"
        const val KEY_NOMBRE = "nombre"
        const val KEY_ROL = "rol"
    }
}