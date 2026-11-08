package org.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import io.ktor.server.http.content.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Cat(val id: Int? = null, val name: String, val color: String)

@Serializable
data class CreateCatRequest(val name: String, val color: String)

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    initDatabase()

    install(ContentNegotiation) {
        json()
    }

    routing {
        staticResources("/", "static")

        get("/api/health") {
            call.response.header("X-Backend-Port", System.getenv("PORT") ?: "8080")
            call.respond(mapOf("status" to "ok"))
        }

        get("/api/cats") {
            val cats = transaction {
                Cats.selectAll()
                    .orderBy(Cats.id, SortOrder.DESC)
                    .map {
                        Cat(it[Cats.id], it[Cats.name], it[Cats.color])
                    }
            }
            call.respond(cats)
        }

        post("/api/cats") {
            val req = call.receive<CreateCatRequest>()
            val name = req.name.trim()
            val color = req.color.trim()

            if (name.isEmpty() || color.isEmpty()) {
                call.respond(mapOf("error" to "name and color are required"))
                return@post
            }

            val newId = transaction {
                Cats.insert {
                    it[Cats.name] = name
                    it[Cats.color] = color
                }[Cats.id]
            }
            call.respond(Cat(newId, name, color))
        }

        delete("/api/cats/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(mapOf("error" to "invalid id"))
                return@delete
            }
            val deleted = transaction {
                Cats.deleteWhere { Cats.id eq id }
            }
            if (deleted > 0) {
                call.respond(mapOf("deleted" to id))
            } else {
                call.respond(mapOf("error" to "not found"))
            }
        }
    }
}