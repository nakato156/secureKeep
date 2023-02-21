package com.example.secretkepp.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.secretkepp.screens.*

@Composable
fun AppNavigation(navController: NavHostController){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.MainScreem.route){
        composable(route= AppScreens.MainScreem.route){
            Principal(navController)
        }
        composable(route= AppScreens.LoginScreen.route){
            Login(navController)
        }
        composable(route= AppScreens.RegisterScreen.route){
            Registro(navController)
        }
        composable(route= AppScreens.AuthScreen.route){
            Auth(navController)
        }
        composable(route= AppScreens.NotesScreen.route + "/{hash}",
            arguments = listOf(navArgument(name="hash"){
                type = NavType.StringType
            })
        ){
            PantallaNotas(navController, it.arguments?.getString("hash"))
        }
    }
}