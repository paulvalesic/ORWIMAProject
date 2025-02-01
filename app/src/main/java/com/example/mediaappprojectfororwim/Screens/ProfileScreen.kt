package com.example.mediaappprojectfororwim.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediaappprojectfororwim.ViewModels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavHostController) {
    val viewModel: ProfileViewModel = viewModel()
    val state = viewModel


    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Chats")
                    }
                },
                title = { Text("Profil", style = MaterialTheme.typography.h6) },
                backgroundColor = Color(0xFF6200EE),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color(0xFF6200EE),
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("profile") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(30.dp))
                    }

                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading.value) {
                CircularProgressIndicator()
            } else {


                Text("Korisničko ime: ${state.username.value}", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Email: ${state.email.value}", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Broj prijatelja: ${state.friendsList.value.size}", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(16.dp))

                if (state.friendsList.value.isNotEmpty()) {
                    Text("Prijatelji:", style = MaterialTheme.typography.body1)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        items(state.friendsList.value) { friend ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = 8.dp,
                                backgroundColor = Color(0xFFF0F0F0),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(friend, style = MaterialTheme.typography.body2)
                                    IconButton(onClick = {
                                        state.removeFriend(friend)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove Friend")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Nemate prijatelja.", style = MaterialTheme.typography.body2)
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (state.isAddingFriend.value) {

                    viewModel.fetchAvailableUsers()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(viewModel.availableUsers.value) { username ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = 8.dp,
                                backgroundColor = Color(0xFFF0F0F0)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(username)
                                    IconButton(
                                        onClick = {
                                            state.friendUsername.value = username
                                            state.addFriend()
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, "Add")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            state.isAddingFriend.value = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
                    ) {
                        Text("Dodaj prijatelja", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE91E63))
                ) {
                    Text("Odjava", color = Color.White)
                }
            }
        }
    }
}
