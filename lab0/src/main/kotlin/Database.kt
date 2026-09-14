package org.example

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import java.time.Instant


object Cats : Table("cats") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val color = varchar("color", 50)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)
}

fun initDatabase() {
    val url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/vet_clinic"
    val user = System.getenv("DB_USER") ?: System.getProperty("user.name")
    val password = System.getenv("DB_PASSWORD") ?: ""

    Database.connect(url, driver = "org.postgresql.Driver", user = user, password = password)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Cats)
    }
}