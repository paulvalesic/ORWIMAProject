package com.example.mediaappprojectfororwim.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mediaappprojectfororwim.ViewModels.ChatsViewModel
import com.example.mediaappprojectfororwim.ViewModels.FriendItem

@Composable
fun Chats(navController: NavController) {
    val viewModel: ChatsViewModel = viewModel()
    val friendUsernames by viewModel.friendUsernames.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Chats")
                    }
                },
                title = { Text("Chats", style = MaterialTheme.typography.h6) },
                backgroundColor = Color(0xFF6200EE),
                modifier = Modifier.statusBarsPadding(),
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
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }

                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else if (friendUsernames.isEmpty()) {
                Text(
                    text = "You have no friends yet.",
                    style = MaterialTheme.typography.h6.copy(color = Color.Gray),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(friendUsernames) { username ->
                        FriendItem(username, navController)
                    }
                }
            }
        }
    }
}




