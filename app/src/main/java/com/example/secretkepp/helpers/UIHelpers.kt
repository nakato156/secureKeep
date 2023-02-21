package com.example.secretkepp.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secretkepp.BD.CurrentUser
import com.example.secretkepp.BD.Nota
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val mainColorGreen = Color(3, 218, 197)

@Composable
fun TopBar(scaffoldState: ScaffoldState, coroutineScope: CoroutineScope){
    TopAppBar(
        title = { Text(text = "Mis Notas") },
        navigationIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    scaffoldState.drawerState.open()
                }
            }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }
    )
}

@Composable
fun CardItem(cardData: Nota, onItemClick: (Nota) -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .clickable { onItemClick(cardData) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(cardData.titulo, fontWeight = FontWeight.Bold)
            Text(cardData.contenido)
        }
    }
}

@Composable
fun createCard(onCardSave: (String, String) -> Unit, onCardClose: () -> Unit, nota: Nota? = null){

    var title by remember { mutableStateOf(nota?.titulo ?: "") }
    var content by remember { mutableStateOf(nota?.contenido ?: "") }

    Card(modifier = Modifier
        .padding(16.dp)
        .height(381.dp)) {
        Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()) {
            Row {
                TextField(
                    modifier= Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("Titulo") },
                )
            }
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(20.dp))
            Row {
                TextField(
                    modifier= Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Escribe algo") }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = {
                        onCardSave(title, content)
                    },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = mainColorGreen)
                ) {
                    Text("Guardar")
                }
                Button(
                    onClick = {
                        onCardClose()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

suspend fun checkPin(coroutineScope: CoroutineScope, hashPin: String): CompletableDeferred<PinResult<Boolean>> {
    val result = CompletableDeferred<PinResult<Boolean>>()
    val user = CurrentUser.user
    coroutineScope.launch {
        val res = user?.existSecret(hashPin)
        res?.let {
            when (res) {
                is FirebaseResult.Success -> {
                    if(res.data) result.complete(PinResult.Success(true))
                    else result.complete(PinResult.Failed(false))
                }
                is FirebaseResult.Failed -> {
                    result.complete(PinResult.Error(res.exception))
                }
            }
        }
    }
    return result
}

@Composable
fun PinWindow(callback: (pin: String, status: Boolean) -> Unit) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {  },
        title = { Text("Introduce tu PIN") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = pin,
                    onValueChange = { if(pin.length < 10) pin = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    textStyle= TextStyle.Default.copy(fontSize = 24.sp),
                    modifier = Modifier
                        .width(140.dp)
                        .padding(top = 1.dp, end = 1.dp)
                )
            }
        },
        dismissButton = { Button(onClick={ callback(pin, false)  }){ Text("Cancelar")} },
        confirmButton = {
            Button(
                onClick = { callback(pin, true) },
                colors = ButtonDefaults.buttonColors(backgroundColor = mainColorGreen)
            ) {
                Text("Aceptar")
            }
        }
    )
}

@Composable
fun ModalCreateSpace(callback: (result:Boolean, pin: String?) -> Unit){
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { callback(false, null) },
        title = { Text("Crear espacio") },
        text={
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    placeholder={ Text("Ingresa un pin") },
                    value = pin,
                    onValueChange = { if(pin.length < 10) pin = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
                Spacer(modifier=Modifier.height(30.dp))
                TextField(
                    placeholder={ Text("Confirme su pin") },
                    value = pinConfirm,
                    onValueChange = { if(pinConfirm.length < 10) pinConfirm = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { callback(pin == pinConfirm, pin) },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = mainColorGreen)
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            Button(
                onClick = { callback(false, null) },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Cerrar")
            }
        }
    )
}