package au.edu.swin.sdmd.vic_turntables

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var linearLayoutManager : LinearLayoutManager
    private lateinit var adapter : MainRowAdapter
    private lateinit var startForResult : ActivityResultLauncher<Intent>
    private val turntableViewModel : TurntableViewModel by viewModels {
        TurntableViewModelFactory((application as TurntableApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //call function to set up recycler view
        setupRecyclerView()

        //call function to register activity result contract with callback
        registerResultsContract()

        //setup FAB to add new turntable
        findViewById<View>(R.id.fabAddNew).setOnClickListener {
            launchAddNewTurntable()
        }

        //setup LiveData observer
        turntableViewModel.allTurntables.observe(this) { turntables ->
            turntables.let { adapter.submitList(it) }
        }

        //populate database on first launch only, detected using shared preferences
        val sharedPref = getPreferences(MODE_PRIVATE)
        if (sharedPref.getBoolean("firstLaunch", true)) {
            lifecycleScope.launch { //launch in coroutine
                readDefaultTurntableData() //populate database with default data from CSV
            }
        }
        //update shared preference so any subsequent launches wont populate database
        with (sharedPref.edit()) {
            putBoolean("firstLaunch", false)
            apply()
        }
    }

    //set up main activity RecyclerView by creating LinearLayoutManager and Adapter
    private fun setupRecyclerView() {
        //get main activity recycler view
        val mainRecyclerView = findViewById<RecyclerView>(R.id.mainTurntableList)

        //create LinearLayoutManager and set RecyclerView layoutManager property
        linearLayoutManager = LinearLayoutManager(this)
        mainRecyclerView.layoutManager = linearLayoutManager

        //create Adapter and set RecyclerView adapter property
        adapter = MainRowAdapter { launchDetailActivity(it) }
        mainRecyclerView.adapter = adapter
    }

    //launch detail activity, called by row adapter click listener
    private fun launchDetailActivity(turntable : TurntableEntity) {
        Log.i("LAUNCH_DETAIL", turntable.name)

        val intent = Intent(this@MainActivity, DetailActivity::class.java)
        intent.apply {
            this.putExtra("turntable", turntable)
        }
        startActivity(intent) //launch detail activity with intent
    }

    //add new turntable by launching edit detail activity
    private fun launchAddNewTurntable() {
        Log.i("LAUNCH_ADD", "New Turntable")

        //create intent with extra
        val intent = Intent(this@MainActivity, EditDetailActivity::class.java)
        intent.apply {
            this.putExtra("method","add") //add new (no TurntableEntity extra)
        }
        startForResult.launch(intent) //launch edit detail activity for result with intent
    }

    //register activity result contract with callback
    private fun registerResultsContract() {
        startForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                //get method string from result data extra
                val resultMethod = result.data!!.getStringExtra("method")
                Log.i("RESULT", "$resultMethod new turntable")

                when (resultMethod) {
                    "save" -> { //save new turntable to database
                        //get TurntableEntity from result data extra
                        val newTurntable = result.data!!
                            .getParcelableExtra<TurntableEntity>("turntable")

                        if (newTurntable != null) {
                            //insert new turntable into database via view model
                            turntableViewModel.insert(newTurntable)

                            //show toast to user that turntable was saved
                            val saveToast = Toast.makeText(
                                this,
                                "${getString(R.string.toastSavedNew)} '${newTurntable.name}'",
                                Toast.LENGTH_LONG
                            )
                            saveToast.show()
                        }
                    }
                    "discard" -> { //discard turntable (show toast)
                        //show toast to user that turntable was discarded
                        val discardToast = Toast.makeText(
                            this,
                            getString(R.string.toastDiscardedNew),
                            Toast.LENGTH_LONG
                        )
                        discardToast.show()
                    }
                    else -> {
                        throw Exception("Result returned without proper 'method' extra")
                    }
                }
            }
        }
    }

    //options menu inflater
    override fun onCreateOptionsMenu(menu : Menu): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu_main, menu)
        return true
    }

    //handle options menu selection
    override fun onOptionsItemSelected(item : MenuItem): Boolean {
        return when (item.itemId) {
            R.id.optionMainAddNew -> { //Add new turntable entry
                Log.i("OPTION_CLICK", "Main Add New")
                launchAddNewTurntable()
                true
            }
            R.id.optionMainDeleteAll -> { //Delete all turntable entries
                Log.i("OPTION_CLICK", "Main Delete All")

                //create and show confirmation dialog
                val confirmDialog = AlertDialog.Builder(this@MainActivity)
                confirmDialog.setTitle(getString(R.string.confirmDeleteAllTitle))
                confirmDialog.setMessage(getString(R.string.confirmNoUndoMessage))
                confirmDialog.setNeutralButton(getString(R.string.confirmCancelButton)) {_, _ ->
                    Log.i("CONFIRM_CLICK", "Cancel")
                } //cancel button
                confirmDialog.setNegativeButton(getString(R.string.confirmDeleteAllButton)) {_, _ -> //delete button
                    Log.i("CONFIRM_CLICK", "Delete All")
                    turntableViewModel.deleteAll() //delete all turntable entities
                }
                confirmDialog.show() //show alert dialog
                true
            }
            R.id.optionMainRefreshData -> { //Replace turntable data with template from csv
                Log.i("OPTION_CLICK", "Main Refresh Data")

                //create and show confirmation dialog
                val confirmDialog = AlertDialog.Builder(this@MainActivity)
                confirmDialog.setTitle(getString(R.string.confirmResetTitle))
                confirmDialog.setMessage(getString(R.string.confirmNoUndoMessage))
                confirmDialog.setNeutralButton(getString(R.string.confirmCancelButton)) {_, _ ->
                    Log.i("CONFIRM_CLICK", "Cancel")
                } //cancel button
                confirmDialog.setNegativeButton(getString(R.string.confirmResetButton)) {_, _ -> //reset button
                    Log.i("CONFIRM_CLICK", "Reset Data")
                    turntableViewModel.deleteAll() //delete all turntable entities
                    lifecycleScope.launch { //launch in coroutine
                        readDefaultTurntableData() //populate database with default data from CSV
                    }
                }
                confirmDialog.show() //show alert dialog
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //read from default_turntable_data.csv and populate database
    private fun readDefaultTurntableData() {
        //open CSV raw resource as input stream then create buffered reader
        val inputStream = resources.openRawResource(R.raw.default_turntable_data) //open raw resource input stream
        val fileReader = inputStream.bufferedReader() //create buffered reader with input stream
        fileReader.readLine() //read header line (not used)

        fileReader.forEachLine {
            val lineList = it.split(",") //split line by comma delimiter
            val gaugeList = lineList[5].split("/").map { gauge -> gauge.toInt()} //split gauges

            //create new TurntableEntity object and populate with CSV data
            val turntable = TurntableEntity(
                lineList[1].toDouble(),    //latitude
                lineList[0].toDouble(),    //longitude
                lineList[2],               //name
                lineList[3],               //location
                lineList[4],               //status
                gaugeList.toMutableList(), //gauges
                lineList[6].toInt(),       //lengthFeet
                lineList[7],               //power
                lineList[8]                //notes
            )
            turntableViewModel.insert(turntable) //insert turntable into Room database
        }

        //close file reader and input stream
        fileReader.close()
        inputStream.close()
    }
}