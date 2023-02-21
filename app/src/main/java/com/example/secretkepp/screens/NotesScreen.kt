package com.example.secretkepp.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.secretkepp.BD.CurrentUser
import com.example.secretkepp.BD.Nota
import com.example.secretkepp.Preferences.appPreferences
import com.example.secretkepp.R
import com.example.secretkepp.helpers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyScreenContent(it: PaddingValues, cardList:MutableList<Nota>) {
    var selectedNote by remember { mutableStateOf<Nota?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn {
        itemsIndexed(cardList) { _, cardData ->
            CardItem(cardData, { selectedNote = it })
        }
    }
    if (selectedNote != null) {
        createCard(
            onCardSave = { titulo, content ->
                val idNote = selectedNote!!.id
                val nota = Nota(idNote, titulo, content, selectedNote!!.fecha)
                coroutineScope.launch {
                    CurrentUser.user?.let{
                        if(it.updateNote(it.currentSpace!!, nota)){
                            val index = cardList.indexOf(selectedNote!!)
                            if(index != -1){
                                cardList[index] = nota
                            }
                        }
                    }
                    selectedNote = null
                }
            },
            onCardClose = { selectedNote = null },
            nota = selectedNote!!,
        )
    }
}

@Composable
fun FloatingButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        val imageModifier = Modifier
            .size(42.dp)
        Image(painter = painterResource(id = R.drawable.image_plus), contentDescription = "img_plus", modifier = imageModifier)
    }
}

@Composable
fun DrawerItem(item: String, accion: ()-> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(6.dp)
            .clip(RoundedCornerShape(12))
    ){
        Image(painterResource(id = R.drawable.image_plus), contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(item,
            modifier= Modifier
                .fillMaxWidth()
                .clickable(onClick = accion),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun Drawer(menuItems: Map<String, () -> Unit>){
    Text("Drawer title", modifier = Modifier.padding(16.dp))
    Divider()
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(4.dp)){
        menuItems.forEach{ (item, accion) ->
            DrawerItem(item, accion)
        }
    }
}

@Composable
fun PantallaNotas(navController: NavController, hash:String?){
    val cardList = remember { mutableStateListOf<Nota>() }
    var showCard by remember { mutableStateOf(false) }
    var actionCreateSpace by remember { mutableStateOf(false) }
    var actionChangeSpace by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val context = navController.context

    val menuItems = mapOf(
        "Cambiar de espacio" to { actionChangeSpace = true },
        "Agregar espacio" to { actionCreateSpace = true },
        "Salir" to { ActivityCompat.finishAffinity(context as Activity) }
    )

    if(hash != "0") CurrentUser.user?.currentSpace = hash
    hash?.let {
        LaunchedEffect(true) {
            cardList.addAll(loadNotes(hash))
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { TopBar(scaffoldState, coroutineScope) },
        content = {
            MyScreenContent(it, cardList)
            if (showCard) {
                Box(modifier = Modifier.fillMaxSize()) {
                    createCard(
                        onCardSave = { title, content ->
                            if(content.trim().isNotEmpty()){
                                cardList.add(Nota(titulo = title, contenido=content))
                                coroutineScope.launch {
                                    CurrentUser.user?.let{ user ->
                                        user.addNote(Nota(titulo=title, contenido=content), user.currentSpace!!)
                                    }
                                }
                                showCard = false
                            }
                        },
                        onCardClose = { showCard = false }
                    )
                }
            }
            else if(actionCreateSpace){
                ModalCreateSpace { result, pin ->
                    if (result && pin != null) {
                        if(pin.isEmpty()) alerta("Por favor ingrese un pin", context)
                        else {
                            val hashPin = hashPassword(pin)
                            createSpace(context, coroutineScope, hashPin, cardList, scaffoldState)
                        }
                    }
                    else if(!result && pin != null) alerta("Las contraseñas no coinciden", context)
                    actionCreateSpace = false
                }
            }
            else if(actionChangeSpace){
                PinWindow{ pin, status ->
                    if(!status) {
                        actionChangeSpace =  false
                        return@PinWindow
                    }

                    val hashPin = hashPassword(pin)
                    coroutineScope.launch {
                        val result = checkPin(coroutineScope, hashPin).await()
                        when(result){
                            is PinResult.Success -> {
                                cardList.clear()
                                cardList.addAll(loadNotes(hashPin))
                                scaffoldState.drawerState.close()
                            }
                            is PinResult.Failed ->{ alerta("No se ha encontrado el espacio deseado", context) }
                            is PinResult.Error -> {
                                alerta("Ha ocurrido un error", context)
                            }
                        }
                        actionChangeSpace =  false
                    }
                }
            }
        },
        floatingActionButton = { FloatingButton {
            if (CurrentUser.user?.currentSpace != null) showCard = true
            else alerta("No está en ningún espacio de trabajo", context)
        } },
        drawerContent = {
            Drawer(menuItems)
        }
    )
}