package au.edu.swin.sdmd.vic_turntables

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var turntableMap : GoogleMap
    private var turntableMarker : Marker? = null
    private var mapReady = false
    private lateinit var turntable : TurntableEntity
    private lateinit var startForResult : ActivityResultLauncher<Intent>
    private val turntableViewModel : TurntableViewModel by viewModels {
        TurntableViewModelFactory((application as TurntableApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //get turntable data from intent extra
        turntable = intent.getParcelableExtra<TurntableEntity>("turntable")!!

        //setup action bar
        supportActionBar!!.apply {
            this.setDisplayHomeAsUpEnabled(true)                 //enable action bar back button
            this.title = getString(R.string.detailActivityTitle) //set action bar title
            this.subtitle = turntable.name                       //set action bar subtitle
        }

        //call function to populate detail activity turntable data TextViews
        populateTurntableData()

        //call function to register activity result contract with callback
        registerResultsContract()

        //setup FAB to edit turntable data
        findViewById<View>(R.id.fabEdit).setOnClickListener {
            launchEditTurntable()
        }

        //get SupportMapFragment and set callback
        Log.i("GOOGLE_MAP", "Map initialising")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.detailMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //populate detail activity turntable data TextViews
    private fun populateTurntableData() {
        findViewById<TextView>(R.id.detailNameData)
            .text = turntable.name
        findViewById<TextView>(R.id.detailLocationData)
            .text = turntable.location
        findViewById<TextView>(R.id.detailStatusData)
            .text = turntable.status
        findViewById<TextView>(R.id.detailGaugeData)
            .text = turntable.gaugeString()
        findViewById<TextView>(R.id.detailLengthData)
            .text = turntable.lengthString()
        findViewById<TextView>(R.id.detailPowerData)
            .text = turntable.power
        findViewById<TextView>(R.id.detailNotesData)
            .text = turntable.notes
    }

    //edit turntable by launching edit detail activity
    private fun launchEditTurntable() {
        Log.i("LAUNCH_EDIT", turntable.name)

        //create intent with extras
        val intent = Intent(this@DetailActivity, EditDetailActivity::class.java)
        intent.apply {
            this.putExtra("method","edit") //add new (no previous data passed in intent)
            this.putExtra("turntable", turntable)
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
                Log.i("RESULT", "$resultMethod changes to turntable")

                when (resultMethod) {
                    "save" -> { //save new turntable to database
                        //get TurntableEntity from result data extra
                        val newTurntable = result.data!!
                            .getParcelableExtra<TurntableEntity>("turntable")

                        if (newTurntable != null) {
                            //set turntable class variable with new TurntableEntity object
                            turntable = newTurntable

                            //update turntable in database via view model
                            turntableViewModel.update(turntable)

                            //update action bar subtitle
                            supportActionBar!!.subtitle = turntable.name

                            //call function to populate detail activity turntable data TextViews
                            populateTurntableData()

                            //move map marker and camera to new lat/long
                            mapMarkerAndCamera()

                            //show toast to user that changes was saved
                            val saveToast = Toast.makeText(
                                this,
                                "${getString(R.string.toastSavedChanges)} '${turntable.name}'",
                                Toast.LENGTH_LONG
                            )
                            saveToast.show()
                        }
                    }
                    "discard" -> { //discard turntable (show toast)
                        //show toast to user that turntable was discarded
                        val discardToast = Toast.makeText(
                            this,
                            getString(R.string.toastDiscardedChanges),
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

    //handle back button pressed
    override fun onBackPressed() {
        Log.i("BACK_CLICK", "Detail Activity")

        super.onBackPressed() //go back to previous activity
    }

    //options menu inflater
    override fun onCreateOptionsMenu(menu : Menu): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu_detail, menu)
        return true
    }

    //handle options menu selection
    override fun onOptionsItemSelected(item : MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() //simulate back button press
                true
            }
            /* R.id.optionDetailEdit -> { //Edit current turntable
                Log.i("OPTION_CLICK", "Detail Edit Item")
                launchEditTurntable()
                true
            } */
            R.id.optionDetailDelete -> { //Delete current turntable
                Log.i("OPTION_CLICK", "Detail Delete Item")

                //create and show confirmation dialog
                val confirmDialog = AlertDialog.Builder(this@DetailActivity)
                confirmDialog.setTitle(getString(R.string.confirmDeleteTitle))
                confirmDialog.setMessage(getString(R.string.confirmNoUndoMessage))
                confirmDialog.setNeutralButton(getString(R.string.confirmCancelButton)) {_, _ -> //cancel button
                    Log.i("CONFIRM_CLICK", "Cancel")
                }
                confirmDialog.setNegativeButton(getString(R.string.confirmDeleteButton)) {_, _ -> //delete button
                    Log.i("CONFIRM_CLICK", "Delete")

                    deleteTurntable() //delete turntable entity
                }
                confirmDialog.show() //show alert dialog
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //delete turntable from database and go back to main activity
    private fun deleteTurntable() {
        turntableViewModel.delete(turntable) //delete turntable
        onBackPressed() //go back to main activity
    }

    //when map is ready populate with turntable location pin
    override fun onMapReady(googleMap: GoogleMap) {
        mapReady = true
        Log.i("GOOGLE_MAP", "Map ready - $googleMap")

        turntableMap = googleMap
        turntableMap.uiSettings.setAllGesturesEnabled(true) //enable map gestures

        //set map style from JSON string resource based on theme
        val mapStyleResource = MapStyleOptions(resources.getString(R.string.googleMapsStyleJson))
        val mapStyleSuccess = turntableMap.setMapStyle(mapStyleResource)
        if (!mapStyleSuccess) { Log.i("GOOGLE_MAP", "Map style parsing failed") }

        mapMarkerAndCamera() //create or move map pin and move camera to pin
    }

    //create or move map pin and move camera
    private fun mapMarkerAndCamera() {
        if (mapReady) {
            //create LatLng object with turntable latitude and longitude
            val turntableLatLng = LatLng(turntable.latitude, turntable.longitude)

            if (turntableMarker != null) { //marker already exists, move marker
                turntableMarker!!.position = turntableLatLng

                //animate camera to pin location
                turntableMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(turntableLatLng, 18f),
                    2000,
                    null
                )
            }
            else { //marker does not exist, create marker
                turntableMarker = turntableMap.addMarker(
                    MarkerOptions().position(turntableLatLng).title(turntable.name)
                )

                //move camera to pin location and zoom in
                turntableMap.moveCamera(CameraUpdateFactory.newLatLngZoom(turntableLatLng, 15f))
                turntableMap.animateCamera(
                    CameraUpdateFactory.zoomTo(18f),
                    2000,
                    null
                )
            }

        }
    }
}