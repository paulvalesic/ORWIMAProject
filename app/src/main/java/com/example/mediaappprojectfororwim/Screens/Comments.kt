package com.example.mediaappprojectfororwim.Screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun Comments(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var friendUsernames by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Function to fetch the current user's friend list and their usernames
    val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference

    if (currentUser != null) {
        LaunchedEffect(currentUser.uid) {
            val friendsRef = database.child("users").child(currentUser.uid).child("friends")
            friendsRef.get().addOnSuccessListener { snapshot ->
                val friendUserIds = snapshot.children.mapNotNull { it.key }
                fetchFriendUsernames(friendUserIds, database) { usernames ->
                    friendUsernames = usernames
                    loading = false
                }
            }.addOnFailureListener {
                Log.e("CommentsScreen", "Error fetching friend list: ${it.message}")
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments") },
                backgroundColor = Color(0xFF808080),
                modifier = Modifier.statusBarsPadding(),

            )
        },
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color(0xFF808080),
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
    ){ paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (loading) {
                // Show loading indicator
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (friendUsernames.isEmpty()) {
                // No friends found
                Text("You have no friends yet.", modifier = Modifier.align(Alignment.Center))
            } else {
                // List of friends
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(friendUsernames) { username ->
                        FriendItem(username,navController)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItem(username: String, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = username)
            IconButton(onClick = {
                // Navigiraj prema ekranu za chat i proslijedi korisničko ime
                navController.navigate("chat_screen/$username")
            }) {
                Icon(Icons.Default.Email, contentDescription = "Message")
            }
        }
    }
}



private fun fetchFriendUsernames(friendUserIds: List<String>, database: DatabaseReference, onResult: (List<String>) -> Unit) {
    val friendUsernames = mutableListOf<String>()

    friendUserIds.forEach { userId ->
        database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val username = snapshot.child("username").getValue(String::class.java)
                username?.let { friendUsernames.add(it) }

                // When all friends have been fetched, call onResult
                if (friendUsernames.size == friendUserIds.size) {
                    onResult(friendUsernames)
                }
            }
        }
    }
}
