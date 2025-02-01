package com.example.mediaappprojectfororwim.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference

    var username = mutableStateOf("")
    var email = mutableStateOf("")
    var friendsList = mutableStateOf<List<String>>(emptyList())
    var isLoading = mutableStateOf(true)
    var isAddingFriend = mutableStateOf(false)
    var friendUsername = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)

    private val userId = auth.currentUser?.uid


    init {
        userId?.let {
            fetchUserProfile(it)
        }
    }

    var availableUsers = mutableStateOf<List<String>>(emptyList())


    fun fetchAvailableUsers() {
        viewModelScope.launch {
            try {
                val currentUsername = username.value
                val currentFriends = friendsList.value

                val snapshot = database.child("users").get().await()
                val users = mutableListOf<String>()

                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue(User::class.java)
                    user?.username?.let { username ->
                        if (username != currentUsername && !currentFriends.contains(username)) {
                            users.add(username)
                        }
                    }
                }
                availableUsers.value = users
            } catch (e: Exception) {
                errorMessage.value = "Error fetching users"
            }
        }
    }

    private fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = database.child("users").child(userId).get().await()
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        username.value = it.username
                        email.value = it.email
                        val friendUserIds = it.friends.keys.toList()
                        fetchFriendUsernames(friendUserIds)
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = "Failed to load profile"
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun fetchFriendUsernames(friendUserIds: List<String>) {
        viewModelScope.launch {
            val friendUsernames = mutableListOf<String>()
            friendUserIds.forEach { userId ->
                try {
                    val snapshot = database.child("users").child(userId).get().await()
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        username?.let { friendUsernames.add(it) }
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Failed to load friends"
                }
            }
            friendsList.value = friendUsernames
        }
    }

    fun addFriend() {
        val usernameToAdd = friendUsername.value.trim()
        if (usernameToAdd.isBlank()) {
            errorMessage.value = "Please enter a username"
            return
        }

        if (usernameToAdd == username.value) {
            errorMessage.value = "You cannot add yourself as a friend"
            return
        }

        if (friendsList.value.contains(usernameToAdd)) {
            errorMessage.value = "You are already friends with this person"
            return
        }

        userId?.let { currentUserId ->
            viewModelScope.launch {
                try {
                    val snapshot = database.child("users").orderByChild("username").equalTo(usernameToAdd).get().await()
                    if (snapshot.exists()) {
                        val friendUserId = snapshot.children.first().key
                        friendUserId?.let {
                            database.child("users").child(currentUserId).child("friends").child(it).setValue(true)
                            database.child("users").child(it).child("friends").child(currentUserId).setValue(true)
                            friendsList.value = friendsList.value + usernameToAdd
                            friendUsername.value = ""
                            isAddingFriend.value = false
                        }
                    } else {
                        errorMessage.value = "Friend not found"
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Error adding friend"
                }
            }
        }
    }

    fun removeFriend(friendUsername: String) {
        userId?.let { currentUserId ->
            viewModelScope.launch {
                try {
                    val snapshot = database.child("users").orderByChild("username").equalTo(friendUsername).get().await()
                    if (snapshot.exists()) {
                        val friendUserId = snapshot.children.first().key
                        friendUserId?.let {
                            database.child("users").child(currentUserId).child("friends").child(it).removeValue()
                            database.child("users").child(it).child("friends").child(currentUserId).removeValue()
                            friendsList.value = friendsList.value.filter { it != friendUsername }
                        }
                    } else {
                        errorMessage.value = "Friend not found"
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Error removing friend"
                }
            }
        }
    }


}
