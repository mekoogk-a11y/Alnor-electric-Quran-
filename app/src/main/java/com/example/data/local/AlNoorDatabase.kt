package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        TafsirCacheEntity::class,
        TranslationCacheEntity::class,
        BookmarkEntity::class,
        MemorizationEntity::class,
        TasbihSessionEntity::class,
        DownloadedAudioEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AlNoorDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: AlNoorDatabase? = null

        fun getInstance(context: Context): AlNoorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AlNoorDatabase {
            return Room.databaseBuilder(
                context,
                AlNoorDatabase::class.java,
                "al_noor_quran.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).quranDao()
                            dao.insertSurahs(SurahSeedData.allSurahs)
                            dao.insertTasbihDefaults(AdhkarSeedData.defaultTasbihPresets)
                            dao.insertMemorizationDefaults(
                                SurahSeedData.allSurahs.map {
                                    MemorizationEntity(
                                        surahId = it.id,
                                        memorizedVersesCount = 0,
                                        totalVerses = it.versesCount,
                                        status = "NOT_STARTED"
                                    )
                                }
                            )
                        }
                    }
                })
                .build()
        }
    }
}
