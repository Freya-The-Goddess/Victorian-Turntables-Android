package au.edu.swin.sdmd.vic_turntables

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditDetailActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var activityMethod : String
    private lateinit var turntable : TurntableEntity
    private lateinit var statusAdapter : ArrayAdapter<CharSequence>
    private lateinit var powerAdapter : ArrayAdapter<CharSequence>
    private lateinit var gaugeAdapter : GaugeSpinnerAdapter
    private var statusSelected : String? = null
    private var powerSelected : String? = null
    private var gaugeSelected : MutableList<Int>? = null
    private var validationPassed : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_detail)

        createSpinners() //create spinners and adapters

        val actionBarTitle : String
        val actionBarSubtitle : String?

        //get method from intent extra
        activityMethod = intent.getStringExtra("method")!!

        when (activityMethod) {
            "add" -> {
                actionBarTitle = getString(R.string.addActivityTitle) //set action bar title for add
                actionBarSubtitle = null

                clearFields() //populate fields with current turntable data
                turntable = createTurntableEntity() //create turntable entity for comparison
            }
            "edit" -> {
                //get turntable data from intent extra
                turntable = intent.getParcelableExtra<TurntableEntity>("turntable")!!

                actionBarTitle = getString(R.string.editActivityTitle) //set action bar title for edit
                actionBarSubtitle = turntable.name //set action bar subtitle

                populateFields() //populate fields with current turntable data
            }
            else -> {
                throw Exception("Edit detail activity launched without proper 'method' intent extra")
            }
        }

        //set up action bar
        supportActionBar!!.apply {
            this.setDisplayHomeAsUpEnabled(true) //enable action bar back button
            this.title = actionBarTitle          //set action bar title
            this.subtitle = actionBarSubtitle    //set action bar subtitle
        }

        //set up on change listeners for text inputs
        createOnChangeListeners()
    }

    //convert length feet to length metres and update view
    @SuppressLint("SetTextI18n")
    private fun convertFeetToMeters() {
        val lengthFeetInputString = findViewById<EditText>(R.id.editLengthInputText) .text.toString()
        var lengthMeters = 0.0

        //if input isn't blank convert feet to metres
        if (lengthFeetInputString != "") {
            lengthMeters = Integer.parseInt(lengthFeetInputString).toDouble() * 0.3048
        }

        //set length metres TextView text
        findViewById<TextView>(R.id.editLengthMeterView).text =
            "${getString(R.string.unitFeet)}   (${String.format("%.2f", lengthMeters)}m)"
    }

    //create spinner adapters and inflate spinner views
    private fun createSpinners() {
        //set up status spinner
        val statusSpinner = findViewById<Spinner>(R.id.editStatusDropdown)
        ArrayAdapter.createFromResource( //create adapter
            this@EditDetailActivity,
            R.array.spinnerStatusArray, //string array of spinner items
            R.layout.spinner_text_item
        ).also { adapter ->
            statusAdapter = adapter
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter //set spinner adapter
        }
        statusSpinner.onItemSelectedListener = this@EditDetailActivity //spinner callback

        //set up power spinner
        val powerSpinner = findViewById<Spinner>(R.id.editPowerDropdown)
        ArrayAdapter.createFromResource( //create adapter
            this@EditDetailActivity,
            R.array.spinnerPowerArray, //string array of spinner items
            R.layout.spinner_text_item
        ).also { adapter ->
            powerAdapter = adapter
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            powerSpinner.adapter = adapter //set spinner adapter
        }
        powerSpinner.onItemSelectedListener = this@EditDetailActivity //spinner callback

        //set up gauge spinner (with custom adapter)
        val gaugeSpinner = findViewById<Spinner>(R.id.editGaugeDropdown)
        GaugeSpinnerAdapter.createFromResource( //create adapter
            this@EditDetailActivity,
            R.array.spinnerGaugeArray, //string array of spinner items
            R.layout.spinner_checkbox_item
        ).also { adapter ->
            gaugeAdapter = adapter
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            gaugeSpinner.adapter = adapter //set spinner adapter
        }
    }

    //set up on change listeners for text inputs
    private fun createOnChangeListeners() {
        //update action bar subtitle and run validation check when name input changes
        findViewById<EditText>(R.id.editNameInputText)
            .addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(name: Editable?) {
                    supportActionBar!!.subtitle = name.toString() //update subtitle
                    inputValidation() //run input validation
                }
                //unused but required overrides from abstract class
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

        //update length metres view when length feet input changes
        findViewById<EditText>(R.id.editLengthInputText)
            .addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(name: Editable?) {
                    convertFeetToMeters()
                }
                //unused but required overrides from abstract class
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

        //run validation check when latitude input changes
        findViewById<EditText>(R.id.editLatitudeInputText)
            .addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(name: Editable?) {
                    inputValidation() //run input validation
                }
                //unused but required overrides from abstract class
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

        //run validation check when longitude input changes
        findViewById<EditText>(R.id.editLongitudeInputText)
            .addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(name: Editable?) {
                    inputValidation() //run input validation
                }
                //unused but required overrides from abstract class
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
    }

    //populate fields with data from TurntableEntity
    private fun populateFields() {
        //populate edit detail activity TextInputEditText views
        findViewById<TextInputEditText>(R.id.editNameInputText)
            .setText(turntable.name)
        findViewById<TextInputEditText>(R.id.editLocationInputText)
            .setText(turntable.location)
        findViewById<TextInputEditText>(R.id.editLatitudeInputText)
            .setText(turntable.latitude.toString())
        findViewById<TextInputEditText>(R.id.editLongitudeInputText)
            .setText(turntable.longitude.toString())
        findViewById<TextInputEditText>(R.id.editLengthInputText)
            .setText(turntable.lengthFeet.toString())
        findViewById<TextInputEditText>(R.id.editNotesInputText)
            .setText(turntable.notes)

        //populate spinner dropdown inputs
        findViewById<Spinner>(R.id.editStatusDropdown)
            .setSelection(statusAdapter.getPosition(turntable.status))
        findViewById<Spinner>(R.id.editPowerDropdown)
            .setSelection(powerAdapter.getPosition(turntable.power))

        //populate gauges multiselect spinner
        gaugeAdapter.setGauges(turntable.gauges)

        //populate variables that store spinner selection
        statusSelected = turntable.status
        powerSelected = turntable.power
        gaugeSelected = turntable.gauges

        //show metres
        convertFeetToMeters()
    }

    //clear or reset input fields
    private fun clearFields() {
        //clear edit detail activity TextInputEditText views
        findViewById<TextInputEditText>(R.id.editNameInputText)
            .setText("")
        findViewById<TextInputEditText>(R.id.editLocationInputText)
            .setText("")
        findViewById<TextInputEditText>(R.id.editLatitudeInputText)
            .setText(0.0.toString())
        findViewById<TextInputEditText>(R.id.editLongitudeInputText)
            .setText(0.0.toString())
        findViewById<TextInputEditText>(R.id.editLengthInputText)
            .setText(0.toString())
        findViewById<TextInputEditText>(R.id.editNotesInputText)
            .setText("")

        //reset spinner dropdown inputs
        findViewById<Spinner>(R.id.editStatusDropdown)
            .setSelection(0)
        findViewById<Spinner>(R.id.editPowerDropdown)
            .setSelection(0)

        //reset gauges multiselect spinner
        gaugeAdapter.setGauges(mutableListOf<Int>())

        //clear variables that store spinner selection
        statusSelected = null
        powerSelected = null
        gaugeSelected = null

        //show metres
        convertFeetToMeters()
    }

    private fun createTurntableEntity(): TurntableEntity {
        //latitude, longitude and length blank input checks
        var lat = findViewById<TextInputEditText>(R.id.editLatitudeInputText)
            .text.toString().toDoubleOrNull()
        if (lat == null) { lat = 0.0 }
        var long = findViewById<TextInputEditText>(R.id.editLongitudeInputText)
            .text.toString().toDoubleOrNull()
        if (long == null) { long = 0.0 }
        var length = findViewById<TextInputEditText>(R.id.editLengthInputText)
            .text.toString().toIntOrNull()
        if (length == null) { length = 0 }

        //spinner input null checks
        var status = ""
        if (statusSelected != null) { status = statusSelected.toString() }
        var power = ""
        if (powerSelected != null) { power = powerSelected.toString() }
        var gauges = mutableListOf<Int>()
        if (gaugeSelected != null) { gauges = gaugeSelected!!.toMutableList() }

        //create new TurntableEntity object from user input values
        val newTurntable = TurntableEntity(
            lat, //latitude
            long, //longitude
            findViewById<TextInputEditText>(R.id.editNameInputText).text.toString(), //name
            findViewById<TextInputEditText>(R.id.editLocationInputText).text.toString(), //location
            status, //status
            gauges, //gauge
            length, //length feet
            power, //power
            findViewById<TextInputEditText>(R.id.editNotesInputText).text.toString() //notes
        )

        //if editing assign the existing UID to the new TurntableEntity object
        if (activityMethod=="edit") { newTurntable.uid = turntable.uid }

        return newTurntable
    }

    //run input validation return boolean of if validation passed
    private fun inputValidation() {
        var passed = true

        //if name is blank or or only whitespaces
        val nameLayout = findViewById<TextInputLayout>(R.id.editNameInputLayout)
        val nameString = findViewById<TextInputEditText>(R.id.editNameInputText).text.toString()
        if (nameString == "") {
            passed = false
            //show validation error to user
            nameLayout.isErrorEnabled = true
            nameLayout.error = getString(R.string.validationNameBlank)
        }
        else {
            //clear error
            nameLayout.error = null
            nameLayout.isErrorEnabled = false
        }

        //if latitude is out of range -90 to 90
        val latLayout = findViewById<TextInputLayout>(R.id.editLatitudeInputLayout)
        var latitude = findViewById<TextInputEditText>(R.id.editLatitudeInputText)
            .text.toString().toDoubleOrNull()
        if (latitude == null) { latitude = 0.0 }

        if (latitude < -90.0 || latitude > 90.0) {
            passed = false
            //show validation error to user
            latLayout.isErrorEnabled = true
            latLayout.error =
                getString(R.string.validationLatRange)
        }
        else {
            //clear error
            latLayout.error = null
            latLayout.isErrorEnabled = false
        }

        //if longitude is out of range -180 to 180
        val longLayout = findViewById<TextInputLayout>(R.id.editLongitudeInputLayout)
        var longitude = findViewById<TextInputEditText>(R.id.editLongitudeInputText)
            .text.toString().toDoubleOrNull()
        if (longitude == null) { longitude = 0.0 }

        if (longitude < -180.0 || longitude > 180.0) {
            passed = false
            //show validation error to user
            longLayout.isErrorEnabled = true
            longLayout.error =
                getString(R.string.validationLongRange)
        }
        else {
            //clear error
            longLayout.error = null
            longLayout.isErrorEnabled = false
        }

        validationPassed = passed //set class scope variable
    }

    //handle back button pressed
    override fun onBackPressed() {
        Log.i("BACK_CLICK", "Edit Detail Activity")

        gaugeSelected = gaugeAdapter.getGaugesList()
        val newTurntable = createTurntableEntity() //create turntable entity from input values
        inputValidation() //run input validation

        //if no changes found go straight back to previous activity
        if (newTurntable == turntable) {
            super.onBackPressed() //go back to previous activity
        }
        //if input validation failed show dialog to fix or discard
        else if (!validationPassed) {
            Log.i("VALIDATION", "Failed")

            //create and show confirmation dialog
            val confirmDialog = AlertDialog.Builder(this@EditDetailActivity)
            confirmDialog.setTitle(getString(R.string.confirmValidationErrorTitle))
            confirmDialog.setMessage(getString(R.string.confirmValidationErrorMessage))
            confirmDialog.setNegativeButton(getString(R.string.confirmDiscardButton)) {_, _ -> //discard button
                Log.i("CONFIRM_CLICK", "Discard")

                //create intent with extra
                val result = Intent().apply {
                    this.putExtra("method","discard") //discard changes (no TurntableEntity extra)
                }

                //set result and go back to previous activity
                setResult(RESULT_OK, result)
                super.onBackPressed()
            }
            confirmDialog.setNeutralButton(getString(R.string.confirmFixButton)) {_, _ -> //cancel button
                Log.i("CONFIRM_CLICK", "Fix problems")
            }
            confirmDialog.show() //show confirmation alert dialog
        }
        //changes detected and validation passed, show dialog to save or discard
        else {
            Log.i("VALIDATION", "Passed")
            //select confirmation alert dialog strings based on activityMethod
            var confirmTitle = ""
            var confirmMessage = ""
            when (activityMethod) {
                "add" -> {
                    confirmTitle = getString(R.string.confirmSaveTitle)
                    confirmMessage = getString(R.string.confirmSaveMessage)
                }
                "edit" -> {
                    confirmTitle = getString(R.string.confirmSaveChangesTitle)
                    confirmMessage = getString(R.string.confirmSaveChangesMessage)
                }
            }

            //create and show confirmation dialog
            val confirmDialog = AlertDialog.Builder(this@EditDetailActivity)
            confirmDialog.setTitle(confirmTitle)
            confirmDialog.setMessage(confirmMessage)
            confirmDialog.setPositiveButton(getString(R.string.confirmSaveButton)) {_, _ -> //save button
                Log.i("CONFIRM_CLICK", "Save")

                //create intent with extras
                val result = Intent().apply {
                    this.putExtra("method","save") //save changes
                    this.putExtra("turntable", newTurntable)
                }

                //set result and go back to previous activity
                setResult(RESULT_OK, result)
                super.onBackPressed()
            }
            confirmDialog.setNegativeButton(getString(R.string.confirmDiscardButton)) {_, _ -> //discard button
                Log.i("CONFIRM_CLICK", "Discard")

                //create intent with extra
                val result = Intent().apply {
                    this.putExtra("method","discard") //discard changes (no TurntableEntity extra)
                }

                //set result and go back to previous activity
                setResult(RESULT_OK, result)
                super.onBackPressed()
            }
            confirmDialog.setNeutralButton(getString(R.string.confirmCancelButton)) {_, _ -> //cancel button
                Log.i("CONFIRM_CLICK", "Cancel")
            }
            confirmDialog.show() //show confirmation alert dialog
        }
    }

    //handle options menu selection
    override fun onOptionsItemSelected(item : MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() //simulate back button press
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //handle spinner item selection
    override fun onItemSelected(
        parent : AdapterView<*>,
        view : View?,
        pos : Int,
        id : Long
    ) {
        when (parent.id) {
            R.id.editStatusDropdown -> { //status spinner selected
                Log.i("SPINNER_CLICK","Status spinner item selected: ${parent.getItemAtPosition(pos)}")

                //set statusSelected to selected string (or null if first option selected)
                statusSelected = if (pos == 0) { null }
                else { parent.getItemAtPosition(pos).toString() }
            }
            R.id.editPowerDropdown -> {
                Log.i("SPINNER_CLICK","Power spinner item selected: ${parent.getItemAtPosition(pos)}")

                //set powerSelected to selected string (or null if first option selected)
                powerSelected = if (pos == 0) { null }
                else { parent.getItemAtPosition(pos).toString() }
            }
        }
    }

    //unused but is required override for AdapterView.OnItemSelectedListener abstract class
    override fun onNothingSelected(parent : AdapterView<*>) {}
}