package com.example.contactguard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.example.chatapplication.ui.navigation.Navigation
import com.example.contactguard.databinding.FragmentAuthSignUpBinding
import com.google.firebase.auth.FirebaseAuth


class AuthSignUpFragment : Fragment(R.layout.fragment_auth_sign_up) {
    private lateinit var binding: FragmentAuthSignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding = FragmentAuthSignUpBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.backSignIn.setOnClickListener {
            Navigation.navigate(this,R.id.authSignInFragment,R.id.authSignInFragment,null)
        }

        binding.sendOtp.setOnClickListener {

            val userEmail = binding.etPhoneNo.text.toString()
            val userPassword = binding.etPassword.text.toString()
            Navigation.navigate(this,R.id.authSignUpFragment,R.id.authSignInFragment,null)

            if(userEmail.isNotBlank() && userPassword.isNotEmpty()){
                registerUser(userEmail,userPassword)
            }
        }
    }

    private fun registerUser(userEmail: String, userPassword: String) {
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Registration success, now send verification email
                    val user = auth.currentUser
                    user?.let {
                        it.sendEmailVerification()
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Navigation.navigate(this,null,R.id.authSignInFragment,null)
                                    Toast.makeText(context, "Verification email sent. Please verify before logging in.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to send verification email: ${emailTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }



}