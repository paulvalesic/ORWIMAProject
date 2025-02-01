package com.example.mediaappprojectfororwim.ViewModels

data class User(
    val username: String = "",
    val email: String = "",
    val friends: Map<String, Boolean> = emptyMap()
)
