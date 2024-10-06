package com.corncob.plugins

import com.corncob.Game
import com.corncob.GameDTO
import com.corncob.User
import com.corncob.UserDTO
import com.mongodb.client.model.Filters.eq
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.flow.firstOrNull

fun generateUUID(): String {
    return java.util.UUID.randomUUID().toString()
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/signup") {
            // Create a new user
            val userDTO = call.receive<UserDTO>()

            val user = User(
                id = generateUUID(),
                username = userDTO.username,
                password = userDTO.password
            )

            // Insert the user
            db.getCollection<User>("users").insertOne(user)

            // Return the user id
            call.respondText(user.id)
        }

        authenticate("auth") {
            get("/secure") {
                call.respondText("Hello Secure World!")
            }

            post("/game/create") {
                // Create a new game instance
                val gameDTO = call.receive<GameDTO>()

                val game = Game(
                    leader = User(id = call.principal<UserIdPrincipal>()!!.name, username = "", password = ""),
                    players = mutableMapOf(),
                    id = generateUUID(),
                    inProgress = true,
                    startTime = gameDTO.startTime,
                    endTime = gameDTO.endTime,
                    timeLimit = gameDTO.timeLimit,
                    location = gameDTO.location,
                )

                // Insert the game
                db.getCollection<Game>("games").insertOne(game);

                // Return the game id
                call.respondText(game.id)
            }

            get("/game/{id}") {
                // Return the game state, based on whether the user is a leader of the game, a player, or not in the game
                val gameId = call.parameters["id"] ?: return@get call.respondText("Invalid game id")

                val game = db.getCollection<Game>("games").find().firstOrNull { it.id == gameId } ?: return@get call.respondText("Game not found")

                call.respond(game);
            }

            post("/game/{id}/join") {
                // Add the current user to the game as a player
                // Get the current user
                val userId = User(id = call.principal<UserIdPrincipal>()!!.name, username = "", password = "")
                val user = db.getCollection<User>("users").find().firstOrNull { it.id == userId.id } ?: return@post call.respondText("User not found")

                // Get the game
                val gameId = call.parameters["id"] ?: return@post call.respondText("Invalid game id")
                val game = db.getCollection<Game>("games").find().firstOrNull { it.id == gameId } ?: return@post call.respondText("Game not found")

                // Add the user to the game
                game.addPlayer(user)

                // Update the game
                db.getCollection<Game>("games").updateOne(filter = eq("id", gameId), update = com.mongodb.client.model.Updates.set("players", game.players));

                // Return the game id
                call.respondText(game.id)
            }

            post("/game/{id}/score") {
                // Set the score of the current user in the game
                // Get the current user
                val userId = User(id = call.principal<UserIdPrincipal>()!!.name, username = "", password = "")
                val user = db.getCollection<User>("users").find().firstOrNull { it.id == userId.id } ?: return@post call.respondText("User not found")

                // Get the game
                val gameId = call.parameters["id"] ?: return@post call.respondText("Invalid game id")
                val game = db.getCollection<Game>("games").find().firstOrNull { it.id == gameId } ?: return@post call.respondText("Game not found")

                // Get the score from the request
                val score = call.receive<Int>()

                // Set the score
                game.setScore(user, score)

                // Update the game
                db.getCollection<Game>("games").updateOne(filter = eq("id", gameId), update = com.mongodb.client.model.Updates.set("players", game.players));

                // Return the game id
                call.respondText(game.id)
            }
        }
    }
}
