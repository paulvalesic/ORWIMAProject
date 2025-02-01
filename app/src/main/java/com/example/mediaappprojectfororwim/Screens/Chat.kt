package com.example.mediaappprojectfororwim.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediaappprojectfororwim.ViewModels.ChatViewModel
import com.example.mediaappprojectfororwim.ViewModels.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ChatScreen(navController: NavController, username: String) {

    val viewModel: ChatViewModel = viewModel()

    LaunchedEffect(username) {
        viewModel.loadReceiverInfoAndMessages(username)
    }

    val receiverUsername = viewModel.receiverUsername.collectAsState().value
    val messages = viewModel.messages.collectAsState().value
    val message = viewModel.message.collectAsState().value
    val isMessageInputVisible = viewModel.isMessageInputVisible.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("chats") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Chats")
                    }
                },
                title = { Text("Chat with $receiverUsername") },
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

                    IconButton(onClick = { viewModel.toggleMessageInputVisibility() }) {
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                items(messages) { message ->
                    MessageItem(message)
                }
            }

            if (isMessageInputVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = message,
                        onValueChange = { viewModel.updateMessage(it) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        label = { Text("Enter message") },
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.sendMessage() }) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val formattedDate = remember(message.timestamp) { formatTimestamp(message.timestamp) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {

            Text(text = message.username, style = MaterialTheme.typography.body2, color = Color.Gray)


            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.content, style = MaterialTheme.typography.body1)


            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formattedDate, style = MaterialTheme.typography.body2, color = Color.Gray)
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}

