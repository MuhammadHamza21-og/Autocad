package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cad_projects")
data class CadProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val clientName: String,
    val siteLocation: String,
    val dateModified: Long,
    val plotDimensionsSummary: String,
    val marlaSummary: String,
    val jsonContent: String
)

@Dao
interface CadProjectDao {
    @Query("SELECT * FROM cad_projects ORDER BY dateModified DESC")
    fun getAllProjects(): Flow<List<CadProjectEntity>>

    @Query("SELECT * FROM cad_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): CadProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: CadProjectEntity)

    @Query("DELETE FROM cad_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

@Database(entities = [CadProjectEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cadProjectDao(): CadProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_draftsman.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
