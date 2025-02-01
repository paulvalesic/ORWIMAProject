package com.example.mediaappprojectfororwim.ViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _successMessage = mutableStateOf("")
    val successMessage: State<String> = _successMessage

    fun registerUser(navController: NavHostController) {
        if (!validateInputs()) {
            return
        }

        auth.createUserWithEmailAndPassword(email.value, password.value)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        _errorMessage.value = "Neuspješan prijenos korisničkog ID-a."
                        return@addOnCompleteListener
                    }

                    val userData = mapOf(
                        "username" to username.value,
                        "email" to email.value,
                        "friends" to mapOf<String, Boolean>()
                    )

                    database.child("users").child(userId).setValue(userData)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                _successMessage.value = "Registracija uspješna!"
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                _errorMessage.value = "Greška pri spremanju podataka u bazu: ${dbTask.exception?.message}"
                            }
                        }
                } else {
                    _errorMessage.value = "Registracija nije uspjela: ${authTask.exception?.message}"
                }
            }
    }

    private fun validateInputs(): Boolean {
        if (username.value.isEmpty() || email.value.isEmpty() || password.value.isEmpty() || confirmPassword.value.isEmpty()) {
            _errorMessage.value = "Sva polja moraju biti popunjena!"
            return false
        }
        if (password.value != confirmPassword.value) {
            _errorMessage.value = "Lozinke se ne podudaraju!"
            return false
        }
        return true
    }

    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }
}