package au.edu.swin.sdmd.vic_turntables

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
//import androidx.sqlite.db.SupportSQLiteDatabase
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch

//Turntable Room database
@Database(entities = [TurntableEntity::class], version = 1, exportSchema = false)
abstract class TurntableDatabase : RoomDatabase() {

    abstract fun turntableDao() : TurntableDao //Data Access Object

    //companion object handles database creation to avoid multiple instances
    companion object {
        @Volatile
        private var INSTANCE : TurntableDatabase? = null

        fun getDatabase(context : Context) : TurntableDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TurntableDatabase::class.java,
                    "TurntableDatabase"
                )
                    //.addCallback(TurntableDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        //database callback is no longer needed
        /*private class TurntableDatabaseCallback(private val scope : CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database.turntableDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(turntableDao: TurntableDao) {
            turntableDao.deleteAll()
        }*/
    }
}