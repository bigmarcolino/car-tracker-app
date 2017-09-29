package com.breakfun.cartracker;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class OptionsActivity extends AppCompatActivity {
    RadioGroup rgGet, rgRajada;
    String getOption, rajadaOption;
    EditText intervalo;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rgGet = (RadioGroup) findViewById(R.id.radioGroupGet);
        rgRajada = (RadioGroup) findViewById(R.id.radioGroupRajada);
        fab = (FloatingActionButton) findViewById(R.id.fabRastrear);

        intervalo = (EditText) findViewById(R.id.intervalo);
        intervalo.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "15") });
        intervalo.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(intervalo.getText().length() == 0) {
                    fab.setVisibility(View.GONE);
                }
                else {
                    fab.setVisibility(View.VISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    public void openMap(View view) {
        Intent mainIntent = new Intent(OptionsActivity.this, MapsActivity.class);

        getOption = ((RadioButton)findViewById(rgGet.getCheckedRadioButtonId())).getText().toString();
        rajadaOption = ((RadioButton)findViewById(rgRajada.getCheckedRadioButtonId())).getText().toString();

        mainIntent.putExtra("getOption", getOption);
        mainIntent.putExtra("rajadaOption", rajadaOption);
        mainIntent.putExtra("intervalo", intervalo.getText().toString());

        OptionsActivity.this.startActivity(mainIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent mainIntent = new Intent(OptionsActivity.this, AboutActivity.class);
            OptionsActivity.this.startActivity(mainIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}