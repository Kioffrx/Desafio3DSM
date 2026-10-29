package com.udb.aprendeapp.model

data class Usuario(
    val id: String? = null,
    val nombre: String,
    val email: String,
    val passwordHash: String,
    val rol: String
)