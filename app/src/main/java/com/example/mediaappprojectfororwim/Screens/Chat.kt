package com.example.mediaappprojectfororwim.Screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val content: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val username: String = ""
)
@Composable
fun ChatScreen(navController: NavController, username: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
        .reference
    val senderId = currentUser?.uid ?: ""
    var receiverId by remember { mutableStateOf("") }
    var receiverUsername by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var senderUsername by remember { mutableStateOf("") }

    // State to control the visibility of the message input area
    var isMessageInputVisible by remember { mutableStateOf(false) }

    // Ensure the user is logged in
    if (currentUser == null) {
        Log.e("ChatScreen", "User is not logged in")
        return
    }

    // Get username of the sender from the database
    LaunchedEffect(senderId) {
        getUsernameFromDatabase(senderId, database) { username ->
            senderUsername = username
        }
    }

    // Load receiver information (ID and username)
    LaunchedEffect(username) {
        Log.d("ChatScreen", "Fetching user ID for username: $username")
        getUserIdByUsername(username, database) { id ->
            receiverId = id
            getUsername(receiverId, database) { receiverName ->
                receiverUsername = receiverName
            }
            // Load all messages when receiver info is available
            loadMessages(senderId, receiverId, database) { loadedMessages ->
                messages = loadedMessages
            }
        }
    }

    // Reload messages when the message list is updated
    LaunchedEffect(messages) {
        Log.d("ChatScreen", "Loading messages after sending or receiving a message")
        loadMessages(senderId, receiverId, database) { loadedMessages ->
            messages = loadedMessages
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with $receiverUsername") },
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
                    // Middle button to toggle message input visibility
                    IconButton(onClick = { isMessageInputVisible = !isMessageInputVisible }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Send Message")
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
            // List of messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Takes remaining space
                    .padding(16.dp)
            ) {
                items(messages) { message ->
                    MessageItem(message)
                }
            }

            // Section for input field and send button (conditionally visible)
            if (isMessageInputVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp), // Optional padding to avoid touching the icon
                        label = { Text("Enter message") },
                        singleLine = true // Ensure the input is single-line
                    )
                    IconButton(onClick = {
                        if (message.isNotBlank() && receiverId.isNotBlank()) {
                            sendMessage(senderId, receiverId, message, senderUsername, database) {
                                // Clear input field after sending the message
                                message = ""
                                loadMessages(senderId, receiverId, database) { loadedMessages ->
                                    messages = loadedMessages
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}



@Composable
fun MessageItem(message: Message) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            // Prikazujemo korisničko ime pošiljatelja
            Text(text = message.username, style = MaterialTheme.typography.body2, color = Color.Gray)

            // Prikazujemo sadržaj poruke
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.content, style = MaterialTheme.typography.body1)

            // Dodajte vrijeme ili druge informacije ako želite
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}

private fun getUserIdByUsername(username: String, database: DatabaseReference, onResult: (String) -> Unit) {
    Log.d("ChatScreen", "Fetching user ID for username: $username")
    database.child("users").orderByChild("username").equalTo(username).get()
        .addOnSuccessListener { snapshot ->
            Log.d("ChatScreen", "Successfully fetched user ID")
            if (snapshot.exists()) {
                val userId = snapshot.children.firstOrNull()?.key ?: ""
                onResult(userId)
            }
        }
        .addOnFailureListener { exception ->
            Log.e("ChatScreen", "Error getting user ID: ${exception.message}")
        }
}

private fun getUsername(userId: String, database: DatabaseReference, onResult: (String) -> Unit) {
    Log.d("ChatScreen", "Fetching username for user ID: $userId")
    database.child("users").child(userId).child("username").get()
        .addOnSuccessListener { snapshot ->
            val username = snapshot.getValue(String::class.java) ?: "Unknown"
            Log.d("ChatScreen", "Successfully fetched username: $username")
            onResult(username)
        }
        .addOnFailureListener { exception ->
            Log.e("ChatScreen", "Error getting username: ${exception.message}")
            onResult("Unknown")
        }
}

private fun getUsernameFromDatabase(userId: String, database: DatabaseReference, onResult: (String) -> Unit) {
    Log.d("ChatScreen", "Fetching username for current user ID: $userId")
    database.child("users").child(userId).child("username").get()
        .addOnSuccessListener { snapshot ->
            val username = snapshot.getValue(String::class.java) ?: "Unknown"
            onResult(username)
        }
        .addOnFailureListener { exception ->
            Log.e("ChatScreen", "Error getting username: ${exception.message}")
            onResult("Unknown")
        }
}

private fun loadMessages(senderId: String, receiverId: String, database: DatabaseReference, onResult: (List<Message>) -> Unit) {
    Log.d("ChatScreen", "Loading messages from Firebase...")
    val chatRef = database.child("chats").child(senderId).child(receiverId).child("messages")
    chatRef.get().addOnSuccessListener { snapshot ->
        Log.d("ChatScreen", "Messages loaded successfully")
        val messages = snapshot.children.mapNotNull { dataSnapshot ->
            val content = dataSnapshot.child("content").getValue(String::class.java)
            val senderId = dataSnapshot.child("senderId").getValue(String::class.java)
            val username = dataSnapshot.child("username").getValue(String::class.java) ?: "Unknown"
            if (content != null && senderId != null) {
                Message(content, senderId, receiverId, username = username)
            } else {
                null
            }
        }
        onResult(messages)
    }.addOnFailureListener { exception ->
        Log.e("ChatScreen", "Error loading messages: ${exception.message}")
    }
}

private fun sendMessage(senderId: String, receiverId: String, messageContent: String, senderUsername: String, database: DatabaseReference, onMessageSent: () -> Unit) {
    val messageId = database.child("chats").child(senderId).child(receiverId).child("messages").push().key
    val timestamp = System.currentTimeMillis()

    val message = mapOf(
        "content" to messageContent,
        "senderId" to senderId,
        "receiverId" to receiverId,
        "timestamp" to timestamp,
        "username" to senderUsername // Ovdje šaljemo username pošiljatelja
    )

    messageId?.let {
        // Zapisujemo poruku u oba smjera (s sender-a na receiver-a i obratno)
        database.child("chats").child(senderId).child(receiverId).child("messages").child(it).setValue(message)
        database.child("chats").child(receiverId).child(senderId).child("messages").child(it).setValue(message)
        onMessageSent() // Poziva funkciju za učitavanje poruka
    }
}
