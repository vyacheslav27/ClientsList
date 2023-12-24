package ru.clientslist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val lastname: String,
    val email: String,
    val number: String,
) : Serializable