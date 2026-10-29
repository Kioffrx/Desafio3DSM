package com.udb.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.udb.aprendeapp.R
import com.udb.aprendeapp.controller.SessionManager
import com.udb.aprendeapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sesion: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sesion = SessionManager(this)

        binding.tvBienvenida.text = getString(R.string.welcome_message)
        binding.tvUsuario.text = getString(R.string.home_usuario_rol, sesion.nombre(), sesion.rol().valor)

        // Solo el Docente ve el acceso a administración (pantalla del Día 2)
        binding.btnAdministrar.visibility = if (sesion.esDocente()) View.VISIBLE else View.GONE

        binding.btnCerrarSesion.setOnClickListener { confirmarLogout() }
    }

    private fun confirmarLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_titulo)
            .setMessage(R.string.logout_mensaje)
            .setPositiveButton(R.string.logout_confirmar) { _, _ ->
                sesion.cerrar()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }
}