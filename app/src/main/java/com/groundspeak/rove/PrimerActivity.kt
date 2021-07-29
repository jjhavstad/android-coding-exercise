package com.groundspeak.rove

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class PrimerActivity : AppCompatActivity() {
    //TODO Request location permission at runtime on API >= 21
    //TODO show magic location settings dialog (if necessary)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primer)
    }
}
