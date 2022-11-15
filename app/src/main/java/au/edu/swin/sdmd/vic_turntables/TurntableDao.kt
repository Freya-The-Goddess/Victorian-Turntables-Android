package au.edu.swin.sdmd.vic_turntables

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TurntableDao {
    @Query("SELECT * FROM TurntableDatabaseTable ORDER BY uid ASC")
    fun getTurntableData() : Flow<List<TurntableEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(turntable : TurntableEntity)

    @Update(entity = TurntableEntity::class)
    suspend fun update(turntable : TurntableEntity)

    @Delete(entity = TurntableEntity::class)
    suspend fun delete(turntable : TurntableEntity)

    @Query("DELETE FROM TurntableDatabaseTable")
    suspend fun deleteAll()
}