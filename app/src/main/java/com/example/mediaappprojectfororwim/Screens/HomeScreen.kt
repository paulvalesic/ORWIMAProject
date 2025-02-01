package com.example.mediaappprojectfororwim.Screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mediaappprojectfororwim.ViewModels.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val posts by viewModel.posts.collectAsState()
    val isAddPostVisible by viewModel.isAddPostVisible.collectAsState()
    val username by viewModel.username.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var newDescription by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }

    if (currentUser != null) {
        LaunchedEffect(currentUser.uid) {
            viewModel.fetchUsername(currentUser.uid)
            viewModel.loadPosts()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                backgroundColor = Color(0xFF6200EE),
                actions = {
                    IconButton(onClick = { navController.navigate("chats") }) {
                        Icon(Icons.Default.Email, contentDescription = "chats")
                    }
                },
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
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                    IconButton(onClick = { viewModel.toggleAddPostVisibility() }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Post")
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (posts.isEmpty()) {
                    Text("No posts yet. Be the first to add one!", modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    posts.forEach { (postId, post) ->
                        PostItem(post = post, postId = postId)
                    }
                }
            }

            if (isAddPostVisible) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newImageUrl,
                        onValueChange = { newImageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (newDescription.isNotEmpty() && newImageUrl.isNotEmpty()) {
                                viewModel.addPost(newDescription, username, newImageUrl)
                                newDescription = ""
                                newImageUrl = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Post")
                    }
                }
            }
        }
    }
}
