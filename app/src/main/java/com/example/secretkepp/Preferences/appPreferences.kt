package com.example.secretkepp.Preferences

import android.content.Context

class appPreferences {
    private val preferences_name = "SecurePreferencesApp"
    private val pref_existNotes = "existe_algunas_notas"
    companion object {
        const val pref_logged = "is_logged_in"
    }

    fun getPrefName(): String {
        return preferences_name
    }

    fun existNotes(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(pref_existNotes, false)
    }

    fun setExistNote(context: Context, status: Boolean = true){
        val sharedPref = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean(pref_existNotes, status)
            apply()
        }
    }

    public fun Login(context: Context, email: String){
        val sharedPref = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean(pref_logged, true)
            putString("email", email)
            apply()
        }
    }

    fun LogOut(context: Context){
        val sharedPref = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }

}