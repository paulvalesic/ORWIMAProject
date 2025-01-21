package com.example.mediaappprojectfororwim.Screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mediaappprojectfororwim.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var friendsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var friendUsername by remember { mutableStateOf(TextFieldValue("")) }
    var loading by remember { mutableStateOf(true) }
    var isAddingFriend by remember { mutableStateOf(false) }

    val userId = auth.currentUser?.uid
    userId?.let {
        database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    username = it.username
                    email = it.email
                    val friendUserIds = it.friends.keys.toList()
                    fetchFriendUsernames(friendUserIds, database) { friendUsernames ->
                        friendsList = friendUsernames
                    }
                }
            }
            loading = false
        }.addOnFailureListener {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                backgroundColor = Color(0xFF808080),
                modifier = Modifier.statusBarsPadding()
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
                    IconButton(
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("profile") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }

                    IconButton(onClick = {}) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text("Profil", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Korisničko ime: $username", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Email: $email", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Broj prijatelja: ${friendsList.size}", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(16.dp))

                if (friendsList.isNotEmpty()) {
                    Text("Prijatelji:", style = MaterialTheme.typography.body1)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp) // Limit height for scrolling
                    ) {
                        items(friendsList) { friend ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = 4.dp
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
                                        removeFriend(friend, database, userId) { success ->
                                            if (success) {
                                                friendsList = friendsList.filter { it != friend }
                                            } else {
                                                Toast.makeText(navController.context, "Greška pri uklanjanju prijatelja", Toast.LENGTH_SHORT).show()
                                            }
                                        }
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

                // Dodaj prijatelja
                if (isAddingFriend) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BasicTextField(
                            value = friendUsername,
                            onValueChange = { friendUsername = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp) // Povećaj visinu za bolji izgled
                                .border(BorderStroke(1.dp, Color.Gray), shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 16.dp), // Dodaj padding unutar textfielda
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    innerTextField()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Poboljšani izgled dugmadi
                        Button(
                            onClick = {
                                if (friendUsername.text.isBlank()) {
                                    Toast.makeText(navController.context, "Molimo unesite korisničko ime", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Provjera da li pokušava dodati sebe kao prijatelja
                                if (friendUsername.text == username) {
                                    Toast.makeText(navController.context, "Ne možete dodati sebe kao prijatelja", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Provjeri da li je prijatelj već na listi
                                if (friendsList.contains(friendUsername.text)) {
                                    Toast.makeText(navController.context, "Već ste prijatelj s ovom osobom", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                addFriend(friendUsername.text, database, userId) { success ->
                                    if (success) {
                                        friendsList = friendsList + friendUsername.text
                                        friendUsername = TextFieldValue("")
                                        isAddingFriend = false
                                    } else {
                                        Toast.makeText(navController.context, "Prijatelj nije pronađen!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)) // Dodaj boju
                        ) {
                            Text("Dodaj prijatelja", color = Color.White) // Tekst u bijeloj boji
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            isAddingFriend = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)) // Dodaj boju
                    ) {
                        Text("Dodaj prijatelja", color = Color.White) // Tekst u bijeloj boji
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE91E63)) // Dodaj boju
                ) {
                    Text("Odjava", color = Color.White) // Tekst u bijeloj boji
                }
            }
        }
    }
}


// Ova funkcija dohvaća korisnička imena svih prijatelja
private fun fetchFriendUsernames(friendUserIds: List<String>, database: DatabaseReference, onResult: (List<String>) -> Unit) {
    val friendUsernames = mutableListOf<String>()

    friendUserIds.forEach { userId ->
        database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val username = snapshot.child("username").getValue(String::class.java)
                username?.let { friendUsernames.add(it) }

                // Kada su svi prijatelji dohvaćeni, pozovemo onResult
                if (friendUsernames.size == friendUserIds.size) {
                    onResult(friendUsernames)
                }
            }
        }
    }
}

// Ova funkcija dodaje prijatelja u bazu podataka
private fun addFriend(friendUsername: String, database: DatabaseReference, userId: String?, onResult: (Boolean) -> Unit) {
    if (userId != null) {
        database.child("users").orderByChild("username").equalTo(friendUsername).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Ako prijatelj postoji, dodaj ga u listu prijatelja
                val friendUserId = snapshot.children.first().key
                friendUserId?.let {
                    database.child("users").child(userId).child("friends").child(it).setValue(true)
                    database.child("users").child(it).child("friends").child(userId).setValue(true)
                    onResult(true)
                } ?: onResult(false)
            } else {
                onResult(false)
            }
        }
    }
}

// Ova funkcija uklanja prijatelja iz baze podataka
private fun removeFriend(friendUsername: String, database: DatabaseReference, userId: String?, onResult: (Boolean) -> Unit) {
    if (userId != null) {
        database.child("users").orderByChild("username").equalTo(friendUsername).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Ako prijatelj postoji, ukloni ga iz liste prijatelja
                val friendUserId = snapshot.children.first().key
                friendUserId?.let {
                    database.child("users").child(userId).child("friends").child(it).removeValue()
                    database.child("users").child(it).child("friends").child(userId).removeValue()
                    onResult(true)
                } ?: onResult(false)
            } else {
                onResult(false)
            }
        }
    }
}
