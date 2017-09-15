package com.test.rodion.androidcodetestrodionmourakhtanov.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.test.rodion.androidcodetestrodionmourakhtanov.R;

/**
 * Activity for editing a contact or for creating a new one. If contact id is passed in this
 * activity it will look up a contact and edit it. If no id is passed it will create a new empty
 * contact and will allow and save it.
 */
public class ContactEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
    }
}
