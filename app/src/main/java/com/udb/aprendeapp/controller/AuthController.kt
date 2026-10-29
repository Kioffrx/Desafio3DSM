package com.udb.aprendeapp.controller

import android.util.Patterns
import com.udb.aprendeapp.R
import com.udb.aprendeapp.model.Rol
import com.udb.aprendeapp.model.Usuario
import com.udb.aprendeapp.model.UsuarioRepository
import java.io.IOException

sealed class ResultadoAuth {
    data class Exito(val usuario: Usuario) : ResultadoAuth()
    data class Error(val mensajes: List<Int>) : ResultadoAuth()
}

class AuthController(
    private val repositorio: UsuarioRepository = UsuarioRepository(),
    private val sesion: SessionManager
) {

    suspend fun registrar(
        nombre: String,
        email: String,
        password: String,
        confirmacion: String,
        rol: Rol
    ): ResultadoAuth {
        val errores = mutableListOf<Int>()
        if (nombre.isBlank()) errores += R.string.error_nombre_vacio
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) errores += R.string.error_email_invalido
        errores += PasswordValidator.validar(password)
        if (password != confirmacion) errores += R.string.error_password_no_coincide
        if (errores.isNotEmpty()) return ResultadoAuth.Error(errores)

        return try {
            if (buscarPorEmailExacto(email) != null) {
                ResultadoAuth.Error(listOf(R.string.error_email_registrado))
            } else {
                val nuevo = Usuario(
                    nombre = nombre.trim(),
                    email = email.trim().lowercase(),
                    passwordHash = HashUtil.sha256(password),
                    rol = rol.valor
                )
                ResultadoAuth.Exito(repositorio.crear(nuevo))
            }
        } catch (e: IOException) {
            ResultadoAuth.Error(listOf(R.string.error_red))
        }
    }

    suspend fun iniciarSesion(email: String, password: String): ResultadoAuth {
        if (email.isBlank() || password.isBlank()) {
            return ResultadoAuth.Error(listOf(R.string.error_campos_vacios))
        }
        return try {
            val usuario = buscarPorEmailExacto(email)
            if (usuario != null && usuario.passwordHash == HashUtil.sha256(password)) {
                sesion.guardar(usuario)
                ResultadoAuth.Exito(usuario)
            } else {
                ResultadoAuth.Error(listOf(R.string.error_credenciales))
            }
        } catch (e: IOException) {
            ResultadoAuth.Error(listOf(R.string.error_red))
        }
    }

    fun cerrarSesion() = sesion.cerrar()

    // MockAPI filtra por "contiene": se confirma la coincidencia exacta aquí
    private suspend fun buscarPorEmailExacto(email: String): Usuario? =
        repositorio.buscarPorEmail(email.trim())
            .firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
}