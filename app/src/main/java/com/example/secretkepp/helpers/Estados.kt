package com.example.secretkepp.helpers

sealed class FirebaseResult<T> {
    class Success<T>(val data: T) : FirebaseResult<T>()
    class Failed<T>(val exception: Exception) : FirebaseResult<T>()
}

sealed class PinResult<T> {
    class Success<T>(val data: T) : PinResult<T>()
    class Failed<T>(val data: T) : PinResult<T>()
    class Error<T>(val exception: Exception) : PinResult<T>()
}