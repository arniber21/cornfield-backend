package com.corncob.plugins

import com.corncob.User
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.coroutines.flow.firstOrNull
import java.io.File


const val connectionString = "mongodb+srv://arnabcare21:meow@cornfield.ucunt.mongodb.net/?retryWrites=true&w=majority&appName=Cornfield"
val client = MongoClient.create(connectionString = connectionString)
val db = client.getDatabase("Cornfield")

fun Application.configureSecurity() {
    install(Authentication) {
        basic("auth") {
            realm = "Ktor Server"
            validate { credentials ->
                // Query mongoDB
                val user = db.getCollection<User>("users").find(Filters.and(listOf(eq("username", credentials.name)))).firstOrNull()

                // Just check the password
                if (user != null && user.password == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}
