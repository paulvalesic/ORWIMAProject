package com.example.mediaappprojectfororwim.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mediaappprojectfororwim.ViewModels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: LoginViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Prijava",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = viewModel.email.value,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.87f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.87f)
            ),
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = viewModel.password.value,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Lozinka") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.87f),
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.87f)
            ),
            shape = MaterialTheme.shapes.medium
        )

        if (viewModel.errorMessage.value.isNotEmpty()) {
            Text(
                text = viewModel.errorMessage.value,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }


        Button(
            onClick = { viewModel.loginUser(navController) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Prijavite se")
        }


        Spacer(modifier = Modifier.height(8.dp))


        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nemate račun? Registrirajte se", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

