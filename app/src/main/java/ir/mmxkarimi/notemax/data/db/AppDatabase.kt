package ir.mmxkarimi.notemax.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.mmxkarimi.notemax.data.entity.NoteEntity

@Database(entities = [NoteEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS notes_new (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            isChecklist INTEGER NOT NULL,
            colorHex INTEGER NOT NULL,
            isPinned INTEGER NOT NULL,
            isArchived INTEGER NOT NULL,
            isTrashed INTEGER NOT NULL,
            createdAt INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL,
            orderIndex INTEGER NOT NULL
          )
          """.trimIndent()
        )
        db.execSQL(
          """
          INSERT INTO notes_new (id, title, content, isChecklist, colorHex, isPinned, isArchived, isTrashed, createdAt, updatedAt, orderIndex)
          SELECT id, titleEncrypted, contentEncrypted, isChecklist, colorHex, isPinned, isArchived, isTrashed, createdAt, updatedAt, orderIndex
          FROM notes
          """.trimIndent()
        )
        db.execSQL("DROP TABLE notes")
        db.execSQL("ALTER TABLE notes_new RENAME TO notes")
      }
    }

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "secure_notes.db"
        )
          .addMigrations(MIGRATION_3_4)
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
