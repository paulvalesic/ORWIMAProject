package com.example.mediaappprojectfororwim.model

data class User(
    val username: String = "",
    val email: String = "",
    val friends: Map<String, Boolean> = emptyMap()  // Lista prijatelja kao mapa s ključem (userId) i vrijednošću (true)
)
