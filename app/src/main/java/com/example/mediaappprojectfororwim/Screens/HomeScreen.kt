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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class Post(
    val username: String = "",
    val description: String = "",
    val likes: Int = 0,
    val likedByUsers: List<String> = listOf(),
    val comments: List<Comment> = listOf(),
    val imageUrl: String? = null // Dodano polje za URL slike
)
data class Comment(
    val username: String = "",
    val comment: String = ""
)

@Composable
fun HomeScreen(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = Color(0xFF808080))

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        Log.e("HomeScreen", "User is not logged in.")
        return
    }

    var username by remember { mutableStateOf("Anonymous") }

    var newDescription by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf(listOf<Pair<String, Post>>()) }

    // State for controlling the visibility of the add post section
    var isAddPostVisible by remember { mutableStateOf(false) }


    LaunchedEffect(currentUser.uid) {
        val db = FirebaseDatabase
            .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child("users")
            .child(currentUser.uid)
            .child("username")

        db.get().addOnSuccessListener { snapshot ->
            username = snapshot.value as? String ?: "Anonymous"
        }
    }




    // Function to load posts from Firebase
    fun loadPosts() {
        val postsRef = FirebaseDatabase
            .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child("posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList = mutableListOf<Pair<String, Post>>()
                snapshot.children.forEach {
                    val postId = it.key ?: return@forEach
                    val post = it.getValue(Post::class.java) ?: return@forEach
                    postList.add(postId to post)
                }
                posts = postList
                Log.d("HomeScreen", "Loaded posts: ${posts.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeScreen", "Error loading posts: ${error.message}")
            }
        })
    }

    // Load posts when entering the screen
    LaunchedEffect(Unit) {
        loadPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                backgroundColor = Color(0xFF808080),
                modifier = Modifier.statusBarsPadding(),
                actions = { IconButton(onClick = { navController.navigate("comments") }) {
                    Icon(Icons.Default.Email, contentDescription = "Comments")
                } }
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
                    // Middle button to toggle add post section visibility
                    IconButton(onClick = { isAddPostVisible = !isAddPostVisible }) {
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
            // Display posts
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

            // Section for adding a new post (conditionally visible)
            if (isAddPostVisible) {
                var newImageUrl by remember { mutableStateOf("") }

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
                            if (newDescription.isNotEmpty()) {
                                addPost(newDescription, username, newImageUrl) // Prosljeđivanje URL-a slike
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
fun addPost(description: String, username: String, imageUrl: String?) {
    val newPost = Post(username = username, description = description, likes = 0, imageUrl = imageUrl)

    val postId = FirebaseDatabase
        .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
        .reference
        .push().key

    if (postId != null) {
        val postData = mapOf(
            "username" to username,
            "description" to description,
            "likes" to 0,
            "likedByUsers" to listOf<String>(),
            "comments" to listOf<Map<String, String>>(),
            "imageUrl" to imageUrl // Dodavanje URL-a slike u bazu
        )

        FirebaseDatabase
            .getInstance("https://orwim-projectmediaapp-default-rtdb.europe-west1.firebasedatabase.app")
            .reference
            .child("posts")
            .child(postId)
            .setValue(postData)
            .addOnSuccessListener {
                Log.d("HomeScreen", "Post successfully added to Firebase")
            }
            .addOnFailureListener { exception ->
                Log.e("HomeScreen", "Error adding post to Firebase", exception)
            }
    }
}



@Composable
fun PostItem(post: Post, postId: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser


    var showMenu by remember { mutableStateOf(false) } // State za otvaranje/zaključavanje menija
    var commentText by remember { mutableStateOf("") }
    var likes by remember { mutableStateOf(post.likes) }
    var likedByUser by remember { mutableStateOf(post.likedByUsers.contains(currentUser?.uid)) }  // Keep track if current user has liked
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
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Ispisivanje username-a i opisa posta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "@${post.username}", style = MaterialTheme.typography.subtitle1)

                // Gumb za tri točkice (postavke)
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                    }

                    // Dropdown meni s opcijama
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false } // Zatvori meni kada se klikne izvan njega
                    ) {
                        if (post.username == username) {
                            DropdownMenuItem(onClick = {
                                deletePost(postId) // Funkcija za brisanje posta
                                showMenu = false // Zatvori meni nakon što se izabere opcija
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.description, style = MaterialTheme.typography.body1)

            // Like dugme i broj lajkova
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {
                    val updatedLikes = if (likedByUser) {
                        likes - 1
                    } else {
                        likes + 1

                    }

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
                    Icon(imageVector = Icons.Default.ThumbUp, contentDescription = "Like",
                        tint = if (likedByUser) Color.Blue else Color.Gray)

                }

                Text(text = "$likes Likes")
                if (likedByUsernames.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(75.dp) // Ograniči visinu prikaza
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            likedByUsernames.forEach { username ->
                                Text(
                                    text = username,
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }
            }

            // Prikaz slike ako postoji
            if (!post.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Komentari
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .height(75.dp) // Ograničenje visine za komentare
                    .verticalScroll(rememberScrollState()) // Omogućuje skrolanje
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    comments.forEach { comment ->
                        Text(
                            text = "${comment.username}: ${comment.comment}",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            // Unos komentara
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Add a comment") },
                modifier = Modifier.fillMaxWidth()
            )

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
                        commentText = "" // Clear comment text after posting
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Comment")
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

// Funkcija za brisanje posta
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



