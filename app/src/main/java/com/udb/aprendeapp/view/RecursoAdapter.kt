package com.udb.aprendeapp.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udb.aprendeapp.R
import com.udb.aprendeapp.databinding.ItemRecursoBinding
import com.udb.aprendeapp.model.Recurso

enum class ModoLista { ESTUDIANTE, LECTURA, ADMIN }

interface RecursoAcciones {
    fun alAlternarFavorito(recurso: Recurso) {}
    fun alCalificar(recurso: Recurso) {}
    fun alEditar(recurso: Recurso) {}
    fun alEliminar(recurso: Recurso) {}
}

fun abrirEnlace(contexto: Context, url: String) {
    try {
        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(contexto, R.string.error_abrir_enlace, Toast.LENGTH_SHORT).show()
    }
}

class RecursoAdapter(
    private val modo: ModoLista,
    private val acciones: RecursoAcciones,
    private val esFavorito: (Recurso) -> Boolean = { false },
    private val calificacionDe: (Recurso) -> Int = { 0 }
) : RecyclerView.Adapter<RecursoAdapter.VH>() {

    private var items: List<Recurso> = emptyList()

    class VH(val binding: ItemRecursoBinding) : RecyclerView.ViewHolder(binding.root)

    fun actualizar(nuevos: List<Recurso>) {
        items = nuevos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRecursoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val recurso = items[position]
        val b = holder.binding
        val ctx = b.root.context

        b.tvTipo.text = recurso.tipo.replaceFirstChar { it.uppercase() }
        b.tvTitulo.text = recurso.titulo
        b.tvDescripcion.text = recurso.descripcion
        b.tvEnlace.text = recurso.enlace
        b.tvId.text = ctx.getString(R.string.recurso_id, recurso.id ?: "-")
        b.tvPromedio.text =
            if (recurso.totalVotos == 0) ctx.getString(R.string.sin_calificaciones)
            else ctx.getString(R.string.promedio_formato, recurso.promedio, recurso.totalVotos)

        Glide.with(b.imgRecurso)
            .load(recurso.imagen)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(b.imgRecurso)

        b.tvEnlace.setOnClickListener { abrirEnlace(ctx, recurso.enlace) }

        b.layoutEstudiante.visibility = if (modo == ModoLista.ESTUDIANTE) View.VISIBLE else View.GONE
        b.layoutAdmin.visibility = if (modo == ModoLista.ADMIN) View.VISIBLE else View.GONE

        if (modo == ModoLista.ESTUDIANTE) {
            b.btnFavorito.setIconResource(
                if (esFavorito(recurso)) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            val voto = calificacionDe(recurso)
            b.btnCalificar.text =
                if (voto > 0) ctx.getString(R.string.btn_recalificar, voto)
                else ctx.getString(R.string.btn_calificar)
            b.btnFavorito.setOnClickListener { acciones.alAlternarFavorito(recurso) }
            b.btnCalificar.setOnClickListener { acciones.alCalificar(recurso) }
        }

        if (modo == ModoLista.ADMIN) {
            b.btnEditar.setOnClickListener { acciones.alEditar(recurso) }
            b.btnEliminar.setOnClickListener { acciones.alEliminar(recurso) }
        }
    }
}