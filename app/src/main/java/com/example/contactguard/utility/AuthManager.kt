package com.example.contactguard.utility
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

object AuthManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    fun isLoggedIn(): Boolean {
        return Firebase.auth.currentUser?.isEmailVerified ?: false
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(context: Context,isLogIn: Boolean){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()){
            putBoolean(KEY_IS_LOGGED_IN,isLogIn)
            apply()
        }
    }
}