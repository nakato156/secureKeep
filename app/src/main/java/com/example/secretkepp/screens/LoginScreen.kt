package com.example.secretkepp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.secretkepp.BD.CurrentUser
import com.example.secretkepp.BD.User
import com.example.secretkepp.Preferences.appPreferences
import com.example.secretkepp.helpers.FirebaseResult
import com.example.secretkepp.helpers.alerta
import com.example.secretkepp.navegacion.AppScreens
import kotlinx.coroutines.launch

@Composable
fun Login(navController: NavController){
    Column(modifier = Modifier
        .background(Color.Black)
        .fillMaxSize(),
        horizontalAlignment= Alignment.CenterHorizontally,
        verticalArrangement= Arrangement.Center
    ) {
        var email by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }

        // campo email
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(text = "Ingrese su correo electronico") } )
        Spacer(Modifier.height(8.dp))

        //campo password
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text(text = "Ingrese su contraseña") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password) )
        Spacer(Modifier.height(8.dp))

        val coroutineScope = rememberCoroutineScope()
        val context = navController.context

        Button(onClick = {
            email = email.trim()
            if(email.equals("") || pass.equals("")){
                alerta("Porfavor rellene todos los campos", navController.context)
            }else{
                coroutineScope.launch {
                    val user = User(email)
                    val res = user.sigIn(email, pass)
                    CurrentUser.user = user
                    when(res){
                        is FirebaseResult.Success -> {
                            appPreferences().Login(context, email)
                            navController.navigate(AppScreens.AuthScreen.route)
                        }
                        is FirebaseResult.Failed -> {
                            alerta(res.exception.message!!, context)
                        }
                    }
                }
            }
        }) {
            Text("Inicar sesion")
        }
    }
}