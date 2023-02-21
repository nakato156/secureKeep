package com.example.secretkepp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.secretkepp.BD.CurrentUser
import com.example.secretkepp.BD.User
import com.example.secretkepp.Preferences.appPreferences
import com.example.secretkepp.helpers.FirebaseResult
import com.example.secretkepp.helpers.alerta
import com.example.secretkepp.navegacion.AppScreens
import kotlinx.coroutines.launch

@Composable
fun Registro(navController: NavController){
    Column(modifier = Modifier
        .background(Color.Black)
        .fillMaxSize(),
        horizontalAlignment= Alignment.CenterHorizontally,
        verticalArrangement= Arrangement.Center
    ) {
        var email by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }
        var passConfirm by remember { mutableStateOf("") }

        // campo email
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(text = "Ingrese su correo electronico") } )
        Spacer(Modifier.height(8.dp))

        //campo password
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text(text = "Ingrese su contraseña") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password) )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = passConfirm, onValueChange = { passConfirm = it }, label = { Text(text = "Confirme su contraseña") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password) )
        Spacer(Modifier.height(8.dp))

        val coroutineScope = rememberCoroutineScope()

        Button(onClick = {
            email = email.trim()
            val context = navController.context
            if(email.equals("") || pass.equals("")){
                alerta("Debe llenar toddos los campos", context)
            }
            else if (!pass.equals(passConfirm)){
                alerta("Las contraseñas no coinciden", context)
            }else {
                coroutineScope.launch {
                    val user = User(email)
                    val res = user.registrar(email, pass, context)
                    CurrentUser.user = user
                    when(res){
                        is FirebaseResult.Success -> {
                            appPreferences().Login(context, email)
                            navController.navigate(AppScreens.AuthScreen.route)
                        }
                        is FirebaseResult.Failed -> {
                            alerta("No se ha podido registrar. ${res.exception.message}", context)
                        }
                    }
                }
            }
        }) {
            Text("Registrarse")
        }
    }
}