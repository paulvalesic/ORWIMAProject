package com.example.mediaappprojectfororwim.ViewModels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    fun loginUser(navController: NavHostController) {
        if (email.value.isEmpty() || password.value.isEmpty()) {
            _errorMessage.value = "Polja za email i lozinku ne smiju biti prazna!"
            return
        }

        auth.signInWithEmailAndPassword(email.value, password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        database.child("users").child(userId).get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    val username = snapshot.child("username").value.toString()
                                    val email = snapshot.child("email").value.toString()

                                    Log.d(
                                        "LoginScreen",
                                        "User Data: Username = $username, Email = $email"
                                    )
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    _errorMessage.value = "Korisnički podaci nisu pronađeni."
                                }
                            }
                    }
                } else {
                    _errorMessage.value = "Prijava nije uspjela: ${task.exception?.message}"
                }
            }
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }
}