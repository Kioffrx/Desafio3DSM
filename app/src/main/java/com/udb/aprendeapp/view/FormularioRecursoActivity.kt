package com.udb.aprendeapp.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.udb.aprendeapp.R
import com.udb.aprendeapp.controller.DatosUsuarioManager
import com.udb.aprendeapp.controller.ErroresFormulario
import com.udb.aprendeapp.controller.RecursoController
import com.udb.aprendeapp.controller.Resultado
import com.udb.aprendeapp.controller.SessionManager
import com.udb.aprendeapp.databinding.ActivityFormularioRecursoBinding
import com.udb.aprendeapp.model.Recurso
import kotlinx.coroutines.launch

class FormularioRecursoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "recurso_id"
    }

    private lateinit var binding: ActivityFormularioRecursoBinding
    private lateinit var controlador: RecursoController
    private var original: Recurso? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sesion = SessionManager(this)
        if (!sesion.esDocente()) {
            Toast.makeText(this, R.string.sin_permiso, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityFormularioRecursoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controlador = RecursoController(DatosUsuarioManager(this, sesion))

        val tipos = resources.getStringArray(R.array.tipos_recurso)
        binding.dropdownTipo.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, tipos))

        val id = intent.getStringExtra(EXTRA_ID)
        if (id == null) {
            binding.tvTituloFormulario.setText(R.string.btn_agregar_recurso)
        } else {
            binding.tvTituloFormulario.setText(R.string.formulario_editar)
            cargarRecurso(id)     // formulario prellenado para edición
        }

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun cargarRecurso(id: String) {
        lifecycleScope.launch {
            mostrarCarga(true)
            when (val resultado = controlador.obtenerPorId(id)) {
                is Resultado.Exito -> {
                    original = resultado.datos
                    binding.etTitulo.setText(resultado.datos.titulo)
                    binding.etDescripcion.setText(resultado.datos.descripcion)
                    binding.dropdownTipo.setText(resultado.datos.tipo, false)
                    binding.etEnlace.setText(resultado.datos.enlace)
                    binding.etImagen.setText(resultado.datos.imagen)
                    mostrarCarga(false)
                }
                is Resultado.Error -> {
                    Toast.makeText(this@FormularioRecursoActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun guardar() {
        val titulo = binding.etTitulo.text.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val tipo = binding.dropdownTipo.text.toString()
        val enlace = binding.etEnlace.text.toString()
        val imagen = binding.etImagen.text.toString()

        val errores = controlador.validarFormulario(titulo, descripcion, tipo, enlace, imagen)
        mostrarErrores(errores)
        if (errores.hayErrores) return

        val actual = original
        lifecycleScope.launch {
            mostrarCarga(true)
            val resultado =
                if (actual == null) controlador.crear(titulo, descripcion, tipo, enlace, imagen)
                else controlador.actualizar(actual, titulo, descripcion, tipo, enlace, imagen)
            mostrarCarga(false)
            when (resultado) {
                is Resultado.Exito -> {
                    Toast.makeText(this@FormularioRecursoActivity, R.string.recurso_guardado, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resultado.Error ->
                    Toast.makeText(this@FormularioRecursoActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarErrores(e: ErroresFormulario) {
        binding.tilTitulo.error = e.titulo?.let { getString(it) }
        binding.tilDescripcion.error = e.descripcion?.let { getString(it) }
        binding.tilTipo.error = e.tipo?.let { getString(it) }
        binding.tilEnlace.error = e.enlace?.let { getString(it) }
        binding.tilImagen.error = e.imagen?.let { getString(it) }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progreso.visibility = if (visible) View.VISIBLE else View.GONE
        binding.btnGuardar.isEnabled = !visible
    }
}