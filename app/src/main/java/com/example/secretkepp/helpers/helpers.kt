package com.example.secretkepp.helpers

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.secretkepp.BD.CurrentUser
import com.example.secretkepp.BD.Nota
import com.example.secretkepp.Preferences.appPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.MessageDigest

fun alerta(message: String, context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(message)
        .setCancelable(false)
        .setPositiveButton("OK") { dialog, id -> dialog.dismiss() }
    val alert = builder.create()
    alert.show()
}

fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun isLogged(context: Context): Boolean {
    val sharedPref = context.getSharedPreferences(appPreferences().getPrefName(), Context.MODE_PRIVATE)
    return sharedPref.getBoolean(appPreferences.pref_logged, false)
}

fun getEmailUserLogged(context: Context):String {
    val sharedPref = context.getSharedPreferences(appPreferences().getPrefName(), Context.MODE_PRIVATE)
    return sharedPref.getString("email", "")!!
}

fun createSpace(context: Context, coroutineScope: CoroutineScope, hashPin: String, cardList: SnapshotStateList<Nota>, scaffoldState: ScaffoldState){
    coroutineScope.launch {
        val res = CurrentUser.user!!.createSpace(hashPin)
        when(res){
            is FirebaseResult.Success -> {
                if(res.data) {
                    CurrentUser.user!!.currentSpace = hashPin
                    appPreferences().setExistNote(context)
                    cardList.clear()
                    scaffoldState.drawerState.close()
                }
                else alerta("Ya existe un espacio con esa clave!", context)
            }
            is FirebaseResult.Failed -> { alerta("Ha ocurrido un error al crear el espacio ${res.exception.message}", context) }
        }
    }
}

suspend fun loadNotes(hash: String): SnapshotStateList<Nota> {
    val cardList = mutableStateListOf<Nota>()
    val res = CurrentUser.user?.getNotesFromHash(hash)!!
    when(res){
        is FirebaseResult.Failed -> { }
        is FirebaseResult.Success -> {
            res.data.notas.forEach{nota ->
                cardList.add(nota)
            }
        }
    }
    return  cardList
}

fun changeSpace(context: Context, coroutineScope: CoroutineScope, hashPin: String, cardList: SnapshotStateList<Nota>){
    coroutineScope.launch {
        when(checkPin(coroutineScope, hashPin).await()){
            is PinResult.Success -> {
                cardList.clear()
                cardList.addAll(loadNotes(hashPin))
                //scaffoldState.drawerState.close()
            }
            is PinResult.Failed ->{ alerta("No se ha encontrado el espacio deseado", context) }
            is PinResult.Error -> {
                alerta("Ha ocurrido un error", context)
            }
        }
    }
}
