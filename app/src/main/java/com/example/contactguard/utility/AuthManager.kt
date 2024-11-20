package com.example.contactguard.utility
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

object AuthManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"


     val  firebaseInstance = FirebaseAuth.getInstance()

    fun isVerified(): Boolean {
        return Firebase.auth.currentUser?.isEmailVerified ?: false
    }

    fun isVerified(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isLogIn(context: Context): Boolean{
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.wtf("From AuthManager -> Login ",prefs.getBoolean(KEY_IS_LOGGED_IN,false).toString())
        return prefs.getBoolean(KEY_IS_LOGGED_IN,false)
    }

    fun setLoggedIn(context: Context){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
//        with(prefs.edit()){
//            putBoolean(KEY_IS_LOGGED_IN,true)
//            apply()
//        }
    }
    fun logout(context: Context){
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear() // Clears all stored data
        editor.apply() // Apply changes asynchronously

    }
}