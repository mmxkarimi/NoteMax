# ProGuard / R8 optimization rules for NoteMax

# Keep app data models, database, DAO, crypto, and ViewModels
-keep class com.example.data.** { *; }
-keep class com.example.crypto.** { *; }
-keep class com.example.ui.viewmodel.** { *; }

# Preserve Room Database and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Keep Compose models and state
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
