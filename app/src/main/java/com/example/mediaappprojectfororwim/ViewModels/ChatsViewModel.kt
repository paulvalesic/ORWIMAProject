package com.example.mediaappprojectfororwim.ViewModels

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference

    private val _friendUsernames = MutableStateFlow<List<String>>(emptyList())
    val friendUsernames: StateFlow<List<String>> = _friendUsernames

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        fetchFriends()
    }

    private fun fetchFriends() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                val friendsRef = database.child("users").child(currentUser.uid).child("friends")
                friendsRef.get().addOnSuccessListener { snapshot ->
                    val friendUserIds = snapshot.children.mapNotNull { it.key }
                    fetchFriendUsernames(friendUserIds)
                }
            }
        } else {
            _loading.value = false
        }
    }

    private fun fetchFriendUsernames(friendUserIds: List<String>) {
        val fetchedUsernames = mutableListOf<String>()
        val totalFriends = friendUserIds.size

        if (totalFriends == 0) {
            _loading.value = false
            _friendUsernames.value = emptyList()
            return
        }

        friendUserIds.forEach { userId ->
            database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").getValue(String::class.java)
                username?.let { fetchedUsernames.add(it) }

                if (fetchedUsernames.size == totalFriends) {
                    _friendUsernames.value = fetchedUsernames
                    _loading.value = false
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
                navController.navigate("chat_screen/$username")
            }) {
                Icon(Icons.Default.Email, contentDescription = "Message")
            }
        }
    }
}

