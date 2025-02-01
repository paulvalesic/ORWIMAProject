package com.example.mediaappprojectfororwim.ViewModels

import androidx.lifecycle.ViewModel
import com.example.mediaappprojectfororwim.Screens.Post
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val _posts = MutableStateFlow<List<Pair<String, Post>>>(emptyList())
    val posts: StateFlow<List<Pair<String, Post>>> = _posts

    private val _username = MutableStateFlow<String>("Anonymous")
    val username: StateFlow<String> = _username

    private val _isAddPostVisible = MutableStateFlow(false)
    val isAddPostVisible: StateFlow<Boolean> = _isAddPostVisible

    fun toggleAddPostVisibility() {
        _isAddPostVisible.value = !_isAddPostVisible.value
    }

    fun loadPosts() {
        val postsRef = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference.child("posts")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = mutableListOf<Pair<String, Post>>()
                snapshot.children.forEach {
                    val postId = it.key ?: return@forEach
                    val post = it.getValue(Post::class.java) ?: return@forEach
                    postList.add(postId to post)
                }
                _posts.value = postList
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchUsername(userId: String) {
        val userRef = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
            .reference.child("users").child(userId).child("username")

        userRef.get().addOnSuccessListener { snapshot ->
            _username.value = snapshot.value as? String ?: "Anonymous"
        }
    }


    fun addPost(description: String, username: String, imageUrl: String?) {
        val newPost = Post(username = username, description = description, likes = 0, imageUrl = imageUrl)

        val postId = FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app").reference.push().key ?: return

        FirebaseDatabase.getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
            .reference.child("posts")
            .child(postId)
            .setValue(newPost)
    }
}
