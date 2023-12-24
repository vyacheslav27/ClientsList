package ru.clientslist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.clientslist.model.Contact

@Dao
interface ContactDao {
    @Insert
    fun insert(contact: Contact): Long

    @Delete
    fun delete(contact: Contact)

    @Query("SELECT * FROM contact")
    fun selectAll(): List<Contact>
}