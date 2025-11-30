package com.drivetree.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.drivetree.app.data.dao.ApplicationDao
import com.drivetree.app.data.dao.BookingDao
import com.drivetree.app.data.dao.InstructorDao
import com.drivetree.app.data.dao.StudentDao
import com.drivetree.app.data.entity.ApplicationEntity
import com.drivetree.app.data.entity.BookingEntity
import com.drivetree.app.data.entity.InstructorEntity
import com.drivetree.app.data.entity.StudentEntity

@Database(
    entities = [InstructorEntity::class, BookingEntity::class, ApplicationEntity::class, StudentEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun instructors(): InstructorDao
    abstract fun bookings(): BookingDao
    abstract fun applications(): ApplicationDao
    abstract fun students(): StudentDao

    companion object {
        @Volatile private var INSTANCE: AppDb? = null
        fun get(context: Context): AppDb =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDb::class.java, "drivetree.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
