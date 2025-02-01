package com.example.mediaappprojectfororwim.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mediaappprojectfororwim.ViewModels.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    val viewModel: RegisterViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Registracija",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.username.value,
            onValueChange = { viewModel.updateUsername(it) },
            label = { Text("Korisničko ime") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.email.value,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.password.value,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Lozinka") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.confirmPassword.value,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = { Text("Potvrdi lozinku") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )



        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.registerUser(navController) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Registriraj se")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (viewModel.errorMessage.value.isNotEmpty()) {
            Text(
                text = viewModel.errorMessage.value,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (viewModel.successMessage.value.isNotEmpty()) {
            Text(
                text = viewModel.successMessage.value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Već imate račun? Prijavite se", style = MaterialTheme.typography.bodyMedium)
        }
    }
}