package com.example.secretkepp.BD

import java.util.*

typealias TypeCastNota = Map<String, Map<String, Map<String, Map<String, String>>>>

data class Nota (
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val contenido: String,
    val fecha: Date = Date()
)

data class ListaNotas(
    val notas: MutableList<Nota>
)