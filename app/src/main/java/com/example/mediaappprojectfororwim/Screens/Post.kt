package com.example.mediaappprojectfororwim.Screens

data class Post(
    val username: String = "",
    val description: String = "",
    val likes: Int = 0,
    val likedByUsers: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val imageUrl: String? = null
)

data class Comment(
    val username: String = "",
    val comment: String = ""
)
