package com.udb.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udb.aprendeapp.R
import com.udb.aprendeapp.controller.AuthController
import com.udb.aprendeapp.controller.ResultadoAuth
import com.udb.aprendeapp.controller.SessionManager
import com.udb.aprendeapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var controlador: AuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sesion = SessionManager(this)
        if (sesion.haySesion()) {
            irAHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controlador = AuthController(sesion = sesion)

        binding.btnIngresar.setOnClickListener { intentarLogin() }
        binding.btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun intentarLogin() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        mostrarCarga(true)
        lifecycleScope.launch {
            val resultado = controlador.iniciarSesion(email, password)
            mostrarCarga(false)
            when (resultado) {
                is ResultadoAuth.Exito -> {
                    Toast.makeText(this@LoginActivity, R.string.login_exitoso, Toast.LENGTH_SHORT).show()
                    irAHome()
                }
                is ResultadoAuth.Error -> {
                    val texto = resultado.mensajes.joinToString("\n") { getString(it) }
                    Toast.makeText(this@LoginActivity, texto, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progreso.visibility = if (visible) View.VISIBLE else View.GONE
        binding.btnIngresar.isEnabled = !visible
    }

    private fun irAHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}