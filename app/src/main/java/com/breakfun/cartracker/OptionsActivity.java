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
    RadioGroup rg_get, rg_burst;
    String get_option, burst_option;
    EditText interval;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rg_get = (RadioGroup) findViewById(R.id.radioGroupGet);
        rg_burst = (RadioGroup) findViewById(R.id.radioGroupRajada);
        fab = (FloatingActionButton) findViewById(R.id.fabRastrear);

        interval = (EditText) findViewById(R.id.intervalo);
        interval.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "15") });
        interval.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(interval.getText().length() == 0)
                    fab.setVisibility(View.GONE);
                else
                    fab.setVisibility(View.VISIBLE);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    public void openMap(View view) {
        Intent main_intent = new Intent(OptionsActivity.this, MapsActivity.class);

        get_option = ((RadioButton)findViewById(rg_get.getCheckedRadioButtonId())).getText().toString();
        burst_option = ((RadioButton)findViewById(rg_burst.getCheckedRadioButtonId())).getText().toString();

        main_intent.putExtra("get_option", get_option);
        main_intent.putExtra("burst_option", burst_option);
        main_intent.putExtra("interval", interval.getText().toString());

        OptionsActivity.this.startActivity(main_intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_about) {
            Intent main_intent = new Intent(OptionsActivity.this, AboutActivity.class);
            OptionsActivity.this.startActivity(main_intent);
        }

        return super.onOptionsItemSelected(item);
    }
}