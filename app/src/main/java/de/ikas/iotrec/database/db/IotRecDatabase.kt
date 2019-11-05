package de.ikas.iotrec.database.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import de.ikas.iotrec.database.dao.*
import de.ikas.iotrec.database.model.*
import de.ikas.iotrec.database.util.ArrayListConverter
import de.ikas.iotrec.database.util.DateTypeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [
    Thing::class, Category::class, Preference::class, Recommendation::class,
    Feedback::class, Rating::class, Experiment::class, Question::class,
    Reply::class
], version = 33)
@TypeConverters(DateTypeConverter::class, ArrayListConverter::class/*, ListConverter::class*/)
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
    abstract fun categoryDao(): CategoryDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun ratingDao(): RatingDao
    abstract fun experimentDao(): ExperimentDao
    abstract fun questionDao(): QuestionDao
    abstract fun replyDao(): ReplyDao

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
        }
    }
}