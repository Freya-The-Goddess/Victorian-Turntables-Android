package au.edu.swin.sdmd.vic_turntables

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

//Repository interacts with database access object
//(and can deal with multiple data sources where applicable, not in this app)
class TurntableRepository(private val turntableDao : TurntableDao) {
    val allTurntables : Flow<List<TurntableEntity>> = turntableDao.getTurntableData()

    //Calls DAO insert function in background thread
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(turntable : TurntableEntity) {
        turntableDao.insert(turntable)
    }

    //Calls DAO update function in background thread
    @WorkerThread
    suspend fun update(turntable : TurntableEntity) {
        turntableDao.update(turntable)
    }

    //Calls DAO delete function in background thread
    @WorkerThread
    suspend fun delete(turntable : TurntableEntity) {
        turntableDao.delete(turntable)
    }

    //Calls DAO deleteAll function in background thread
    @WorkerThread
    suspend fun deleteAll() {
        turntableDao.deleteAll()
    }
}