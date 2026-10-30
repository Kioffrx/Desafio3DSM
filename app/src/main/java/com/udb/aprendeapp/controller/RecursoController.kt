package com.udb.aprendeapp.controller

import android.util.Patterns
import com.udb.aprendeapp.R
import com.udb.aprendeapp.model.Recurso
import com.udb.aprendeapp.model.RecursoRepository
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.text.Normalizer

enum class OrdenRecursos {
    TITULO_ASC, TITULO_DESC, MEJOR_CALIFICADOS, TIPO;

    companion object {
        fun desdeIndice(indice: Int): OrdenRecursos = entries.getOrElse(indice) { TITULO_ASC }
    }
}

data class ErroresFormulario(
    val titulo: Int? = null,
    val descripcion: Int? = null,
    val tipo: Int? = null,
    val enlace: Int? = null,
    val imagen: Int? = null
) {
    val hayErrores: Boolean
        get() = listOf(titulo, descripcion, tipo, enlace, imagen).any { it != null }
}

class RecursoController(
    private val datos: DatosUsuarioManager,
    private val repositorio: RecursoRepository = RecursoRepository()
) {

    // ---------- API ----------
    suspend fun cargar(): Resultado<List<Recurso>> = ejecutar { repositorio.obtenerTodos() }

    suspend fun obtenerPorId(id: String): Resultado<Recurso> = ejecutar { repositorio.obtenerPorId(id) }

    suspend fun crear(
        titulo: String, descripcion: String, tipo: String, enlace: String, imagen: String
    ): Resultado<Recurso> = ejecutar {
        repositorio.crear(
            Recurso(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                tipo = tipo.trim().lowercase(),
                enlace = enlace.trim(),
                imagen = imagen.trim()
            )
        )
    }

    suspend fun actualizar(
        original: Recurso, titulo: String, descripcion: String, tipo: String, enlace: String, imagen: String
    ): Resultado<Recurso> = ejecutar {
        val id = original.id ?: throw IOException("Recurso sin id")
        repositorio.actualizar(
            id,
            original.copy(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                tipo = tipo.trim().lowercase(),
                enlace = enlace.trim(),
                imagen = imagen.trim()
            )
        )
    }

    suspend fun eliminar(id: String): Resultado<Unit> = ejecutar { repositorio.eliminar(id) }

    // Calificación: el promedio vive en el backend (suma y total de votos).
    // Si el usuario ya había votado, se reemplaza su voto anterior en vez de sumar otro.
    suspend fun calificar(recurso: Recurso, estrellas: Int): Resultado<Recurso> = ejecutar {
        val id = recurso.id ?: throw IOException("Recurso sin id")
        val actual = repositorio.obtenerPorId(id)          // datos frescos de otros usuarios
        val previa = datos.calificacionDe(id)
        val actualizado = actual.copy(
            sumaCalificaciones = actual.sumaCalificaciones - previa + estrellas,
            totalVotos = if (previa == 0) actual.totalVotos + 1 else actual.totalVotos
        )
        val guardado = repositorio.actualizar(id, actualizado)
        datos.guardarCalificacion(id, estrellas)
        guardado
    }

    // ---------- Favoritos ----------
    fun esFavorito(recurso: Recurso): Boolean = datos.esFavorito(recurso.id)

    fun alternarFavorito(recurso: Recurso): Boolean =
        recurso.id?.let { datos.alternarFavorito(it) } ?: false

    fun calificacionDe(recurso: Recurso): Int = datos.calificacionDe(recurso.id)

    // ---------- Búsqueda, filtro y orden ----------
    fun filtrarYOrdenar(
        recursos: List<Recurso>,
        consulta: String,
        tipo: String?,
        soloFavoritos: Boolean,
        orden: OrdenRecursos
    ): List<Recurso> {
        val q = normalizar(consulta)
        val idBuscado = consulta.trim()
        val favoritos = if (soloFavoritos) datos.favoritos() else emptySet()

        val filtrados = recursos.filter { r ->
            val coincideTipo = tipo == null || normalizar(r.tipo) == normalizar(tipo)
            val coincideFavorito = !soloFavoritos || (r.id ?: "") in favoritos
            val coincideBusqueda = q.isEmpty() ||
                    r.id == idBuscado ||
                    normalizar(r.titulo).contains(q) ||
                    normalizar(r.tipo).contains(q)
            coincideTipo && coincideFavorito && coincideBusqueda
        }

        return when (orden) {
            OrdenRecursos.TITULO_ASC -> filtrados.sortedBy { normalizar(it.titulo) }
            OrdenRecursos.TITULO_DESC -> filtrados.sortedByDescending { normalizar(it.titulo) }
            OrdenRecursos.MEJOR_CALIFICADOS -> filtrados.sortedByDescending { it.promedio }
            OrdenRecursos.TIPO -> filtrados.sortedWith(
                compareBy({ normalizar(it.tipo) }, { normalizar(it.titulo) })
            )
        }
    }

    // ---------- Validación del formulario ----------
    fun validarFormulario(
        titulo: String, descripcion: String, tipo: String, enlace: String, imagen: String
    ): ErroresFormulario = ErroresFormulario(
        titulo = if (titulo.trim().length !in 3..80) R.string.error_titulo else null,
        descripcion = if (descripcion.trim().length < 10) R.string.error_descripcion else null,
        tipo = if (tipo.isBlank()) R.string.error_tipo else null,
        enlace = if (!esUrlValida(enlace)) R.string.error_enlace else null,
        imagen = if (!esUrlValida(imagen)) R.string.error_imagen else null
    )

    private fun esUrlValida(url: String): Boolean {
        val limpia = url.trim()
        return (limpia.startsWith("http://") || limpia.startsWith("https://")) &&
                Patterns.WEB_URL.matcher(limpia).matches()
    }

    // Quita tildes y mayúsculas para que "articulo" encuentre "Artículo"
    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    private suspend fun <T> ejecutar(bloque: suspend () -> T): Resultado<T> =
        try {
            Resultado.Exito(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Resultado.Error(R.string.error_red)
        } catch (e: Exception) {
            Resultado.Error(R.string.error_inesperado)
        }
}