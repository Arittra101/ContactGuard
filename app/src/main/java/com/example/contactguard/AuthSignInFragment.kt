package com.example.contactguard

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.example.chatapplication.ui.navigation.Navigation


import com.example.contactguard.databinding.FragmentAuthSignInBinding
import com.example.contactguard.utility.AuthManager.isLoggedIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException


class AuthSignInFragment : Fragment(R.layout.fragment_auth_sign_in) {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentAuthSignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        val currentFragment = R.id.authSignInFragment
        val destinationFragment = R.id.authSignUpFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        auth = FirebaseAuth.getInstance()
        binding = FragmentAuthSignInBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)


        if(isLoggedIn()) navigationToHome()

        binding.sendOtp.setOnClickListener {
            val userEmail = binding.etPhoneNo.text.toString()
            val userPassword = binding.etPassword.text.toString()
            if (userEmail.isNotBlank() && userPassword.isNotEmpty()) {
                loginUser(userEmail, userPassword)
            }
        }

        binding.forgetPass.setOnClickListener {
            val userEmail = binding.etPhoneNo.text.toString()
            if(userEmail.isNotBlank()){
                forgetPass(userEmail)
                
            }
        }
        binding.createAccount.setOnClickListener {
            Navigation.navigate(this, currentFragment, destinationFragment, null)
        }
    }

    private fun forgetPass(userEmail: String) {
        auth.sendPasswordResetEmail(userEmail)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // If the reset email was sent successfully
                    Toast.makeText(requireContext(), "Reset password email sent. Check your inbox.", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle failure
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        // The user is not registered
                        Toast.makeText(requireContext(), "Email not registered. Please create an account first.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle other errors
                        Toast.makeText(requireContext(), "Error: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }


    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        navigationToHome()
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Please verify your email before logging in.",
                            Toast.LENGTH_SHORT
                        ).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
   private  fun navigationToHome(){
        val intent = Intent(requireContext(), AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }


}
