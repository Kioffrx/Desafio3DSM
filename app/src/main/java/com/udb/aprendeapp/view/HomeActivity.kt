package com.udb.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.udb.aprendeapp.R
import com.udb.aprendeapp.controller.DatosUsuarioManager
import com.udb.aprendeapp.controller.OrdenRecursos
import com.udb.aprendeapp.controller.RecursoController
import com.udb.aprendeapp.controller.Resultado
import com.udb.aprendeapp.controller.SessionManager
import com.udb.aprendeapp.databinding.ActivityHomeBinding
import com.udb.aprendeapp.databinding.DialogCalificarBinding
import com.udb.aprendeapp.model.Recurso
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), RecursoAcciones {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sesion: SessionManager
    private lateinit var controlador: RecursoController
    private lateinit var adaptador: RecursoAdapter

    private var todos: List<Recurso> = emptyList()
    private var tipoSeleccionado: String? = null
    private var soloFavoritos = false
    private var orden = OrdenRecursos.TITULO_ASC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesion = SessionManager(this)
        controlador = RecursoController(DatosUsuarioManager(this, sesion))

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle =
            getString(R.string.home_usuario_rol, sesion.nombre(), sesion.rol().valor)

        // El Estudiante interactúa (favorito y calificar); el Docente solo consulta aquí
        val modo = if (sesion.esDocente()) ModoLista.LECTURA else ModoLista.ESTUDIANTE
        adaptador = RecursoAdapter(
            modo, this,
            esFavorito = { controlador.esFavorito(it) },
            calificacionDe = { controlador.calificacionDe(it) }
        )
        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = adaptador

        binding.etBuscar.doAfterTextChanged { aplicarFiltros() }
        configurarOrden()
    }

    override fun onResume() {
        super.onResume()
        cargarRecursos()
    }

    // ---------- Menú ----------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menu.findItem(R.id.accion_favoritos).isVisible = !sesion.esDocente()
        menu.findItem(R.id.accion_administrar).isVisible = sesion.esDocente()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.accion_favoritos -> {
            soloFavoritos = !soloFavoritos
            item.setIcon(if (soloFavoritos) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            Toast.makeText(
                this,
                if (soloFavoritos) R.string.mostrando_favoritos else R.string.mostrando_todos,
                Toast.LENGTH_SHORT
            ).show()
            aplicarFiltros()
            true
        }
        R.id.accion_administrar -> {
            startActivity(Intent(this, AdminRecursosActivity::class.java))
            true
        }
        R.id.accion_cerrar_sesion -> {
            confirmarLogout()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // ---------- Carga ----------
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
                todos = resultado.datos
                construirChips()
                aplicarFiltros()
            }
            is Resultado.Error ->
                Toast.makeText(this, resultado.mensaje, Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progreso.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) binding.tvVacio.visibility = View.GONE
    }

    // ---------- Filtros ----------
    private fun configurarOrden() {
        val opciones = resources.getStringArray(R.array.opciones_orden)
        binding.dropdownOrden.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, opciones))
        binding.dropdownOrden.setText(opciones[0], false)
        binding.dropdownOrden.setOnItemClickListener { _, _, posicion, _ ->
            orden = OrdenRecursos.desdeIndice(posicion)
            aplicarFiltros()
        }
    }

    private fun construirChips() {
        val grupo = binding.chipsTipo
        grupo.setOnCheckedStateChangeListener(null)
        grupo.removeAllViews()

        val tipos = todos.map { it.tipo.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().sorted()
        val actual = tipoSeleccionado
        if (actual != null && actual !in tipos) tipoSeleccionado = null

        grupo.addView(crearChip(getString(R.string.filtro_todos), null, tipoSeleccionado == null))
        tipos.forEach { tipo ->
            grupo.addView(
                crearChip(tipo.replaceFirstChar { it.uppercase() }, tipo, tipo == tipoSeleccionado)
            )
        }

        grupo.setOnCheckedStateChangeListener { g, ids ->
            val chip = ids.firstOrNull()?.let { g.findViewById<Chip>(it) }
            tipoSeleccionado = chip?.tag as? String
            aplicarFiltros()
        }
    }

    private fun crearChip(texto: String, tipo: String?, marcado: Boolean): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_tipo, binding.chipsTipo, false) as Chip
        chip.id = View.generateViewId()
        chip.text = texto
        chip.tag = tipo
        chip.isChecked = marcado
        return chip
    }

    private fun aplicarFiltros() {
        val lista = controlador.filtrarYOrdenar(
            todos, binding.etBuscar.text.toString(), tipoSeleccionado, soloFavoritos, orden
        )
        adaptador.actualizar(lista)
        binding.tvVacio.setText(if (soloFavoritos) R.string.no_hay_favoritos else R.string.no_hay_resultados)
        binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    // ---------- Acciones de la tarjeta ----------
    override fun alAlternarFavorito(recurso: Recurso) {
        val agregado = controlador.alternarFavorito(recurso)
        Toast.makeText(
            this,
            if (agregado) R.string.favorito_agregado else R.string.favorito_quitado,
            Toast.LENGTH_SHORT
        ).show()
        aplicarFiltros()
    }

    override fun alCalificar(recurso: Recurso) {
        val vista = DialogCalificarBinding.inflate(layoutInflater)
        vista.ratingBar.rating = controlador.calificacionDe(recurso).toFloat()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.calificar_titulo, recurso.titulo))
            .setView(vista.root)
            .setPositiveButton(R.string.calificar_confirmar) { _, _ ->
                val estrellas = vista.ratingBar.rating.toInt()
                if (estrellas < 1) {
                    Toast.makeText(this, R.string.error_calificacion, Toast.LENGTH_SHORT).show()
                } else {
                    enviarCalificacion(recurso, estrellas)
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun enviarCalificacion(recurso: Recurso, estrellas: Int) {
        lifecycleScope.launch {
            mostrarCarga(true)
            when (val resultado = controlador.calificar(recurso, estrellas)) {
                is Resultado.Exito -> {
                    Toast.makeText(this@HomeActivity, R.string.calificacion_exitosa, Toast.LENGTH_SHORT).show()
                    recargar()
                }
                is Resultado.Error ->
                    Toast.makeText(this@HomeActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
            }
            mostrarCarga(false)
        }
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