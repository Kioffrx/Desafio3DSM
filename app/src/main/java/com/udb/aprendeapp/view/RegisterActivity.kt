package com.udb.aprendeapp.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udb.aprendeapp.R
import com.udb.aprendeapp.controller.AuthController
import com.udb.aprendeapp.controller.ResultadoAuth
import com.udb.aprendeapp.controller.SessionManager
import com.udb.aprendeapp.databinding.ActivityRegisterBinding
import com.udb.aprendeapp.model.Rol
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var controlador: AuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controlador = AuthController(sesion = SessionManager(this))

        binding.btnRegistrar.setOnClickListener { intentarRegistro() }
        binding.btnVolver.setOnClickListener { finish() }
    }

    private fun intentarRegistro() {
        val rol = if (binding.rbDocente.isChecked) Rol.DOCENTE else Rol.ESTUDIANTE
        mostrarCarga(true)
        lifecycleScope.launch {
            val resultado = controlador.registrar(
                nombre = binding.etNombre.text.toString(),
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                confirmacion = binding.etConfirmacion.text.toString(),
                rol = rol
            )
            mostrarCarga(false)
            when (resultado) {
                is ResultadoAuth.Exito -> {
                    Toast.makeText(this@RegisterActivity, R.string.registro_exitoso, Toast.LENGTH_LONG).show()
                    finish()
                }
                is ResultadoAuth.Error -> {
                    val texto = resultado.mensajes.joinToString("\n") { getString(it) }
                    Toast.makeText(this@RegisterActivity, texto, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progreso.visibility = if (visible) View.VISIBLE else View.GONE
        binding.btnRegistrar.isEnabled = !visible
    }
}