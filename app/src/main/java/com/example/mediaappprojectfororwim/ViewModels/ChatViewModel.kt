package com.example.mediaappprojectfororwim.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
        .reference


    private val _senderId = MutableStateFlow<String>(auth.currentUser?.uid ?: "")
    val senderId: StateFlow<String> = _senderId

    private val _receiverId = MutableStateFlow<String>("")
    val receiverId: StateFlow<String> = _receiverId

    private val _receiverUsername = MutableStateFlow<String>("")
    val receiverUsername: StateFlow<String> = _receiverUsername

    private val _senderUsername = MutableStateFlow<String>("")
    val senderUsername: StateFlow<String> = _senderUsername

    private val _message = MutableStateFlow<String>("")
    val message: StateFlow<String> = _message

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isMessageInputVisible = MutableStateFlow<Boolean>(false)
    val isMessageInputVisible: StateFlow<Boolean> = _isMessageInputVisible

    init {
        auth.currentUser?.uid?.let { userId ->
            getUsernameFromDatabase(userId)
        }
    }


    private fun getUsernameFromDatabase(userId: String) {
        database.child("users").child(userId).child("username").get()
            .addOnSuccessListener { snapshot ->
                _senderUsername.value = snapshot.getValue(String::class.java) ?: "Anonymous"
            }
    }


    fun loadReceiverInfoAndMessages(username: String) {
        viewModelScope.launch {
            getUserIdByUsername(username) { id ->
                _receiverId.value = id
                getUsername(_receiverId.value) { receiverName ->
                    _receiverUsername.value = receiverName
                }
                loadMessages()
            }
        }
    }


    private fun getUserIdByUsername(username: String, onResult: (String) -> Unit) {
        database.child("users").orderByChild("username").equalTo(username).get()
            .addOnSuccessListener { snapshot ->
                val userId = snapshot.children.firstOrNull()?.key ?: ""
                onResult(userId)
            }

    }


    private fun getUsername(userId: String, onResult: (String) -> Unit) {
        database.child("users").child(userId).child("username").get()
            .addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java) ?: "Unknown"
                onResult(username)
            }

    }


    private fun loadMessages() {
        val chatRef = database.child("chats").child(_senderId.value).child(_receiverId.value).child("messages")
        chatRef.get().addOnSuccessListener { snapshot ->
            val loadedMessages = snapshot.children.mapNotNull { dataSnapshot ->
                val content = dataSnapshot.child("content").getValue(String::class.java)
                val senderId = dataSnapshot.child("senderId").getValue(String::class.java)
                val username = dataSnapshot.child("username").getValue(String::class.java) ?: "Unknown"
                val timestamp = dataSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                if (content != null && senderId != null) {
                    Message(content, senderId, _receiverId.value, username = username,  timestamp = timestamp)
                } else {
                    null
                }
            }
            _messages.value = loadedMessages
        }
    }


    fun sendMessage() {
        if (_message.value.isNotBlank() && _receiverId.value.isNotBlank()) {
            val messageId = database.child("chats").child(_senderId.value).child(_receiverId.value).child("messages").push().key
            val timestamp = System.currentTimeMillis()

            val messageData = mapOf(
                "content" to _message.value,
                "senderId" to _senderId.value,
                "receiverId" to _receiverId.value,
                "timestamp" to timestamp,
                "username" to _senderUsername.value
            )

            messageId?.let {

                database.child("chats").child(_senderId.value).child(_receiverId.value).child("messages").child(it).setValue(messageData)
                database.child("chats").child(_receiverId.value).child(_senderId.value).child("messages").child(it).setValue(messageData)
                _message.value = ""
                loadMessages()
            }
        }
    }

    fun toggleMessageInputVisibility() {
        _isMessageInputVisible.value = !_isMessageInputVisible.value
    }

    fun updateMessage(newMessage: String) {
        _message.value = newMessage
    }
}



data class Message(
    val content: String,
    val senderId: String,
    val receiverId: String,
    val username: String,
    val timestamp: Long
)


