package de.ikas.iotrec.database.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.model.Category
//import de.ikas.iotrec.database.dao.VenueDao
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.util.DateTypeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Thing::class, Category::class], version = 8)
@TypeConverters(DateTypeConverter::class)
public abstract class IotRecDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: IotRecDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope // get a scope to launch coroutines
        ): IotRecDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create database here
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IotRecDatabase::class.java,
                    "iotrec_database"
                )
                    .addCallback(IotRecDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun thingDao(): ThingDao
    //abstract fun venueDao(): VenueDao
    abstract fun categoryDao(): CategoryDao


    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearAllTables() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private class IotRecDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.thingDao())
                }
            }
        }

        // when starting the app, remove all Things from the database (we have to re-scan for nearby Things)
        fun populateDatabase(thingDao: ThingDao) {
            thingDao.deleteAll()

            /*
            val thing = Thing(
                "bd03a13f-54c7-4443-b6a3-93fbdcd5ff03-1-2",
                "bla bla bla",
                "",
                "bd03a13f-54c7-4443-b6a3-93fbdcd5ff03",
                1,
                2,
                "bluetooth name",
                1.25,
                123,
                "mac address",
                5,
                50
            )
            thingDao.insert(thing)
            */
        }
    }
}