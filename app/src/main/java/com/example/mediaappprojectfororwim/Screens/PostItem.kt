package com.example.mediaappprojectfororwim.Screens

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun PostItem(post: Post, postId: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var showMenu by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var likes by remember { mutableStateOf(post.likes) }
    var likedByUser by remember { mutableStateOf(post.likedByUsers.contains(currentUser?.uid)) }
    var comments by remember { mutableStateOf(post.comments) }
    var likedByUsernames by remember { mutableStateOf<List<String>>(emptyList()) }
    var username by remember { mutableStateOf("Anonymous") }


    if (currentUser != null) {
        LaunchedEffect(currentUser.uid) {
            val db = FirebaseDatabase
                .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
                .reference
                .child("users")
                .child(currentUser.uid)
                .child("username")

            db.get().addOnSuccessListener { snapshot ->
                username = snapshot.value as? String ?: "Anonymous"
            }.addOnFailureListener { exception ->
                Log.e("HomeScreen", "Error fetching username: ${exception.message}")
            }
        }
    }

    LaunchedEffect(post.likedByUsers) {
        fetchUsernamesForLikes(post.likedByUsers) { usernames ->
            likedByUsernames = usernames
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(horizontal = 16.dp),
        elevation = 6.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "@${post.username}", style = MaterialTheme.typography.subtitle1)

                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (post.username == username) {
                            DropdownMenuItem(onClick = {
                                deletePost(postId)
                                showMenu = false
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = "Description: ${post.description}",
                style = MaterialTheme.typography.body1.copy(
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val updatedLikes = if (likedByUser) likes - 1 else likes + 1
                    val updatedLikedByUsers = if (likedByUser) {
                        post.likedByUsers.filterNot { it == currentUser?.uid }
                    } else {
                        post.likedByUsers + currentUser?.uid.orEmpty()
                    }

                    FirebaseDatabase
                        .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
                        .reference
                        .child("posts")
                        .child(postId)
                        .child("likes")
                        .setValue(updatedLikes)

                    FirebaseDatabase
                        .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
                        .reference
                        .child("posts")
                        .child(postId)
                        .child("likedByUsers")
                        .setValue(updatedLikedByUsers)

                    likes = updatedLikes
                    likedByUser = !likedByUser
                }) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = if (likedByUser) Color(0xFF6200EE) else Color.Gray
                    )
                }

                Text(text = "$likes Likes", style = MaterialTheme.typography.body2)


                if (likedByUsernames.isNotEmpty()) {
                    Box(modifier = Modifier.height(75.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            likedByUsernames.forEach { username ->
                                Text(text = username, style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                }
            }


            if (!post.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                )
            }


            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Comments", style = MaterialTheme.typography.body2)

            Box(modifier = Modifier.height(100.dp).verticalScroll(rememberScrollState())) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    comments.forEach { comment ->
                        Text(
                            text = "${comment.username}: ${comment.comment}",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }


            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Add a comment") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (commentText.isNotEmpty()) {
                        val newComment = Comment(username = username, comment = commentText)
                        val updatedComments = post.comments + newComment

                        FirebaseDatabase
                            .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
                            .reference
                            .child("posts")
                            .child(postId)
                            .child("comments")
                            .setValue(updatedComments)

                        comments = updatedComments
                        commentText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
            ) {
                Text("Add Comment", color = Color.White)
            }
        }
    }
}



fun fetchUsernamesForLikes(uids: List<String>, callback: (List<String>) -> Unit) {
    val usersRef = FirebaseDatabase
        .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
        .reference
        .child("users")

    val usernames = mutableListOf<String>()
    var remaining = uids.size

    if (uids.isEmpty()) {
        callback(usernames)
        return
    }

    uids.forEach { uid ->
        usersRef.child(uid).child("username").get().addOnSuccessListener { snapshot ->
            val username = snapshot.value as? String ?: "Unknown"
            usernames.add(username)
            remaining--
            if (remaining == 0) {
                callback(usernames)
            }
        }.addOnFailureListener {
            remaining--
            if (remaining == 0) {
                callback(usernames)
            }
        }
    }
}


fun deletePost(postId: String) {
    FirebaseDatabase
        .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
        .reference
        .child("posts")
        .child(postId)
        .removeValue()
        .addOnSuccessListener {
            Log.d("HomeScreen", "Post successfully deleted.")
        }
        .addOnFailureListener { exception ->
            Log.e("HomeScreen", "Error deleting post: ${exception.message}")
        }
}
