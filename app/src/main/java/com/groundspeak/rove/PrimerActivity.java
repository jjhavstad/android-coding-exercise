package com.groundspeak.rove;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class PrimerActivity extends AppCompatActivity {

    //TODO Request location permission at runtime on API >= 21
    //TODO show magic location settings dialog (if necessary)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primer);
    }
}
