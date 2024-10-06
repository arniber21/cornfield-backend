package com.corncob

import kotlinx.serialization.Serializable

data class User(val id: String, val username: String, val password: String) {

}

@Serializable
data class UserDTO(val username: String, val password: String) {

}