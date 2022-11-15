package au.edu.swin.sdmd.vic_turntables

import android.app.Application
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.SupervisorJob

//Application class
class TurntableApplication : Application() {
    //val applicationScope = CoroutineScope(SupervisorJob())

    //database and repository created by lazy so they are only created when needed
    val database by lazy { TurntableDatabase.getDatabase(this) }
    val repository by lazy { TurntableRepository(database.turntableDao()) }
}