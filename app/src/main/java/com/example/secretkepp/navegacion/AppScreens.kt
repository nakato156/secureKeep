package com.example.secretkepp.navegacion

sealed class AppScreens(val route: String){
    object  MainScreem: AppScreens("main_screen")
    object  LoginScreen: AppScreens("login_screen")
    object  RegisterScreen: AppScreens("register_screen")
    object  AuthScreen: AppScreens("auth_screen")
    object  NotesScreen: AppScreens("notes_screen")
}
