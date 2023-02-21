package com.example.secretkepp.BD

import android.content.Context
import android.util.Log
import com.example.secretkepp.helpers.FirebaseResult
import com.example.secretkepp.helpers.alerta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object CurrentUser {
    var user: User? = null
}

class User(val email:String) {
    private val myCollection = "Users"
    private val db = FirebaseFirestore.getInstance()
    var currentSpace: String? = null

    suspend fun registrar(email: String, pwd: String, context: Context?): FirebaseResult<Boolean> = try {
        val snapshot_auth = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pwd).await()
        if(snapshot_auth.user == null){
            val msg = snapshot_auth.toString()
            when(context) { context!! -> alerta("Ha ourrido un error $msg", context)}
            FirebaseResult.Success(false)
        }
        else {
            db.collection(myCollection).document(email).set(hashMapOf(
                "email" to email,
                "pwd" to pwd
            )).await()
            FirebaseResult.Success(true)
        }
    } catch (e: Exception) {
        FirebaseResult.Failed(e)
    }

    suspend fun sigIn(email: String, pwd: String): FirebaseResult<Boolean> = try{
        val auth = FirebaseAuth.getInstance()
        val snapshot = auth.signInWithEmailAndPassword(email, pwd).await()
        Log.d("status", "ok")
        Log.d("status", snapshot.user.toString())
        FirebaseResult.Success(snapshot.user == null)
    } catch (e: Exception) {
        Log.d("Errorrrrr", e.message.toString())
        FirebaseResult.Failed(e)
    }

    suspend fun hasNotes(): FirebaseResult<Boolean> = try {
        // verifica si el usaurio tiene notas guardadas
        val snapshot = db.collection(myCollection).document(this.email).get().await()
        val notas = snapshot.get("notas") as TypeCastNota
        FirebaseResult.Success(notas.isNotEmpty())
    } catch (e: Exception) {
        FirebaseResult.Failed(e)
    }

    suspend fun addNote(nota: Nota, hash:String): FirebaseResult<Boolean> = try {
        val data = mapOf(
            "titulo" to nota.titulo,
            "contenido" to nota.contenido,
            "fecha" to nota.fecha
        )
        db.collection(myCollection).document(this.email).update("notas.${hash}.${nota.id}", data).await()
        FirebaseResult.Success(true)
    } catch (e: Exception) {
        FirebaseResult.Failed(e)
    }

    suspend fun updateNote(hash:String, nota: Nota): Boolean = try {
        val data = mapOf(
            "titulo" to nota.titulo,
            "contenido" to nota.contenido,
            "fecha" to nota.fecha
        )
        db.collection(myCollection).document(this.email).update("notas.$hash.${nota.id}", data).await()
        true
    } catch (e: Exception){
        false
    }

    suspend fun getNotesFromHash(hash: String): FirebaseResult<ListaNotas> = try {
        val snapshot = db.collection(myCollection).document(this.email).get().await()
        val notas: MutableList<Nota> = mutableListOf()
        val mapNotas = snapshot.get("notas") as TypeCastNota
        val mapSecret = mapNotas[hash]

        for ((clave, data) in mapSecret!!) {
            val titulo: String = data.get("titulo").toString()
            val contenido: String = data.get("contenido").toString()
            val nota = Nota(clave, titulo, contenido)
            notas.add(nota)
        }

        val lista = ListaNotas(notas = notas)
        FirebaseResult.Success(lista)
    } catch (e: Exception) {
      FirebaseResult.Failed(e)
    }

    suspend fun createSpace(hash: String): FirebaseResult<Boolean> = try {
        val result = existSecret(hash)
        when(result){
            is FirebaseResult.Success -> {
                if(result.data) FirebaseResult.Success(false)
                else {
                    val data = mapOf<String, Map<String, String>>()
                    db.collection(myCollection).document(this.email).update("notas.${hash}", data).await()
                    FirebaseResult.Success(true)
                }
            }
            is FirebaseResult.Failed -> { FirebaseResult.Success(false) }

        }
    } catch (e: Exception){
        FirebaseResult.Failed(e)
    }

    suspend fun existSecret(hash: String):FirebaseResult<Boolean> = try {
        val snapshot = db.collection(myCollection).document(this.email).get().await()
        val notas = snapshot.get("notas") as TypeCastNota?
        FirebaseResult.Success(notas != null && notas.containsKey(hash))
    } catch (e: Exception){
        FirebaseResult.Failed(e)
    }

    suspend fun getUser(): FirebaseResult<List<String>> = try {
        val snapshot = db.collection(myCollection).document(this.email).get().await()
        val data = snapshot.get("list_data") as List<String>
        FirebaseResult.Success(data)
    } catch (e: Exception) {
        FirebaseResult.Failed(e)
    }
}
