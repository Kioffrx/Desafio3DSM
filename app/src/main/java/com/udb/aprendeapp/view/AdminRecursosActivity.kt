package com.udb.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.udb.aprendeapp.R
import com.udb.aprendeapp.controller.DatosUsuarioManager
import com.udb.aprendeapp.controller.RecursoController
import com.udb.aprendeapp.controller.Resultado
import com.udb.aprendeapp.controller.SessionManager
import com.udb.aprendeapp.databinding.ActivityAdminRecursosBinding
import com.udb.aprendeapp.model.Recurso
import kotlinx.coroutines.launch

// Pantalla exclusiva del Docente: crear (POST), editar (PUT) y eliminar (DELETE)
class AdminRecursosActivity : AppCompatActivity(), RecursoAcciones {

    private lateinit var binding: ActivityAdminRecursosBinding
    private lateinit var controlador: RecursoController
    private lateinit var adaptador: RecursoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sesion = SessionManager(this)
        if (!sesion.esDocente()) {
            Toast.makeText(this, R.string.sin_permiso, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityAdminRecursosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controlador = RecursoController(DatosUsuarioManager(this, sesion))

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.btn_administrar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adaptador = RecursoAdapter(ModoLista.ADMIN, this)
        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = adaptador

        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, FormularioRecursoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (::controlador.isInitialized) cargarRecursos()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun cargarRecursos() {
        lifecycleScope.launch {
            mostrarCarga(true)
            recargar()
            mostrarCarga(false)
        }
    }

    private suspend fun recargar() {
        when (val resultado = controlador.cargar()) {
            is Resultado.Exito -> {
                adaptador.actualizar(resultado.datos)
                binding.tvVacio.visibility = if (resultado.datos.isEmpty()) View.VISIBLE else View.GONE
            }
            is Resultado.Error ->
                Toast.makeText(this, resultado.mensaje, Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progreso.visibility = if (visible) View.VISIBLE else View.GONE
        binding.fabAgregar.isEnabled = !visible
    }

    override fun alEditar(recurso: Recurso) {
        startActivity(
            Intent(this, FormularioRecursoActivity::class.java)
                .putExtra(FormularioRecursoActivity.EXTRA_ID, recurso.id)
        )
    }

    // Confirmación obligatoria antes de eliminar
    override fun alEliminar(recurso: Recurso) {
        AlertDialog.Builder(this)
            .setTitle(R.string.eliminar_titulo)
            .setMessage(getString(R.string.eliminar_mensaje, recurso.titulo))
            .setPositiveButton(R.string.eliminar_confirmar) { _, _ -> eliminar(recurso) }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun eliminar(recurso: Recurso) {
        val id = recurso.id ?: return
        lifecycleScope.launch {
            mostrarCarga(true)
            when (val resultado = controlador.eliminar(id)) {
                is Resultado.Exito -> {
                    Toast.makeText(this@AdminRecursosActivity, R.string.recurso_eliminado, Toast.LENGTH_SHORT).show()
                    recargar()
                }
                is Resultado.Error ->
                    Toast.makeText(this@AdminRecursosActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
            }
            mostrarCarga(false)
        }
    }
}