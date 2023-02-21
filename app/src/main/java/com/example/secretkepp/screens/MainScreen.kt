package com.example.secretkepp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.secretkepp.BD.CurrentUser
import com.example.secretkepp.BD.User
import com.example.secretkepp.helpers.getEmailUserLogged
import com.example.secretkepp.helpers.isLogged
import com.example.secretkepp.navegacion.AppScreens

@Composable
fun Principal(navController: NavController){
    if(isLogged(navController.context)){
        CurrentUser.user = User(getEmailUserLogged(navController.context))
        return navController.navigate(AppScreens.AuthScreen.route)
    }

    Column(modifier = Modifier
        .background(Color.Black)
        .fillMaxSize(),
        horizontalAlignment= Alignment.CenterHorizontally,
        verticalArrangement= Arrangement.Center
    ){
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(AppScreens.LoginScreen.route)
        }){
            Text("Ingresar")
        }
        Button(onClick = {
            navController.navigate(AppScreens.RegisterScreen.route)
        }){
            Text("Registrarse")
        }
    }
}