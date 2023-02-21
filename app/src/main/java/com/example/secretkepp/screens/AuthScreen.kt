package com.example.secretkepp.screens

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.secretkepp.BD.CurrentUser
import com.example.secretkepp.Preferences.appPreferences
import com.example.secretkepp.helpers.*
import com.example.secretkepp.navegacion.AppScreens
import kotlinx.coroutines.launch

private var canAuthenticate = false
private lateinit var promprInfo: BiometricPrompt.PromptInfo

private fun setupAuth(context: Context){
    if(BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS){
        canAuthenticate = true

        promprInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biometrica")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }
}

private fun autenticar(fragment: FragmentActivity, context: Context, auth: (auth: Boolean) -> Unit){
    if(canAuthenticate){
        BiometricPrompt(fragment, ContextCompat.getMainExecutor(context),
            object: BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    auth(true)
                }
            }).authenticate(promprInfo)
    } else {
        auth(true)
    }
}

@Composable
fun Auth(navController: NavController) {
    setupAuth(navController.context)
    var  auth by remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    var showPinWindow by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val current = LocalContext.current
    val fragment = current as FragmentActivity

    BackHandler {
        val activity = current as Activity
        ActivityCompat.finishAffinity(activity)
    }
    Column(modifier = Modifier
        .background(Color.Black)
        .fillMaxSize(),
        horizontalAlignment= Alignment.CenterHorizontally,
        verticalArrangement= Arrangement.Center
    ){
        Text("Se necesita Autenticación", fontSize=22.sp)
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            autenticar(fragment, navController.context) {
                auth = it
                if (auth){
                    isLoading.value= true
                    coroutineScope.launch {
                        when(val result = CurrentUser.user?.hasNotes()) {
                            is FirebaseResult.Success -> {
                                if(result.data) showPinWindow = true
                                else navController.navigate(AppScreens.NotesScreen.route + "/0")
                            }
                            is FirebaseResult.Failed -> { }
                            else -> { }
                        }
                        isLoading.value = false
                    }
                }
            }
        }){
            Text("Autenticar")
        }
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).width(60.dp).height(60.dp)
            )
        }

        if(showPinWindow){
            AlertDialog(
                onDismissRequest = { showPinWindow = false },
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
                                .padding(top=1.dp, end=1.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            isLoading.value = true
                            coroutineScope.launch {
                                showPinWindow = false
                                val hashPin = hashPassword(pin)
                                val res = checkPin(coroutineScope, hashPin).await()
                                when(res){
                                    is PinResult.Success -> {
                                        navController.navigate(AppScreens.NotesScreen.route + "/$hashPin")
                                    }
                                    is PinResult.Failed -> { alerta("Pin incorrecto", current) }
                                    is PinResult.Error -> { alerta("Ha ocurrido un error al ingresar", current) }
                                }
                                isLoading.value = false
                                pin = ""
                            }
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}