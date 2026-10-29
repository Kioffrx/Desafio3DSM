package com.udb.aprendeapp.model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("usuarios")
    suspend fun buscarUsuariosPorEmail(@Query("email") email: String): Response<List<Usuario>>

    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: Usuario): Response<Usuario>

    @GET("recursos")
    suspend fun obtenerRecursos(): Response<List<Recurso>>

    @GET("recursos/{id}")
    suspend fun obtenerRecurso(@Path("id") id: String): Response<Recurso>

    @POST("recursos")
    suspend fun crearRecurso(@Body recurso: Recurso): Response<Recurso>

    @PUT("recursos/{id}")
    suspend fun actualizarRecurso(@Path("id") id: String, @Body recurso: Recurso): Response<Recurso>

    @DELETE("recursos/{id}")
    suspend fun eliminarRecurso(@Path("id") id: String): Response<Recurso>
}