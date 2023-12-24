package ru.clientslist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.clientslist.model.Contact

@Database(entities = [Contact::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun contactsDao(): ContactDao
}