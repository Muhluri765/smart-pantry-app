package com.richfield.smartpantryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Screen 5: Settings Activity managing user preferences via Android SharedPreferences.
 * Controls expiring-soon alert toggles, threshold days, and measurement unit system.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "SmartPantryPreferences";
    public static final String KEY_EXPIRY_ALERTS = "pref_expiry_alerts";
    public static final String KEY_EXPIRY_THRESHOLD = "pref_expiry_threshold";
    public static final String KEY_UNIT_SYSTEM = "pref_unit_system";

    private Toolbar toolbarSettings;
    private Switch switchExpiryAlerts;
    private RadioGroup radioGroupExpiryDays;
    private RadioButton radio3Days;
    private RadioButton radio5Days;
    private RadioButton radio7Days;
    private RadioGroup radioGroupUnits;
    private RadioButton radioMetric;
    private RadioButton radioImperial;
    private Button btnSaveSettings;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        setupToolbar();
        loadPreferences();
        setupListeners();
    }

    private void initViews() {
        toolbarSettings = findViewById(R.id.toolbarSettings);
        switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        radioGroupExpiryDays = findViewById(R.id.radioGroupExpiryDays);
        radio3Days = findViewById(R.id.radio3Days);
        radio5Days = findViewById(R.id.radio5Days);
        radio7Days = findViewById(R.id.radio7Days);
        radioGroupUnits = findViewById(R.id.radioGroupUnits);
        radioMetric = findViewById(R.id.radioMetric);
        radioImperial = findViewById(R.id.radioImperial);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarSettings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadPreferences() {
        boolean alertsEnabled = sharedPreferences.getBoolean(KEY_EXPIRY_ALERTS, true);
        int thresholdDays = sharedPreferences.getInt(KEY_EXPIRY_THRESHOLD, 3);
        String unitSystem = sharedPreferences.getString(KEY_UNIT_SYSTEM, "metric");

        switchExpiryAlerts.setChecked(alertsEnabled);
        setThresholdGroupEnabled(alertsEnabled);

        if (thresholdDays == 7) {
            radio7Days.setChecked(true);
        } else if (thresholdDays == 5) {
            radio5Days.setChecked(true);
        } else {
            radio3Days.setChecked(true);
        }

        if ("imperial".equalsIgnoreCase(unitSystem)) {
            radioImperial.setChecked(true);
        } else {
            radioMetric.setChecked(true);
        }
    }

    private void setupListeners() {
        switchExpiryAlerts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setThresholdGroupEnabled(isChecked);
            }
        });

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
            }
        });
    }

    private void setThresholdGroupEnabled(boolean enabled) {
        radioGroupExpiryDays.setEnabled(enabled);
        radio3Days.setEnabled(enabled);
        radio5Days.setEnabled(enabled);
        radio7Days.setEnabled(enabled);
    }

    private void savePreferences() {
        boolean alertsEnabled = switchExpiryAlerts.isChecked();

        int thresholdDays = 3;
        int checkedThresholdId = radioGroupExpiryDays.getCheckedRadioButtonId();
        if (checkedThresholdId == R.id.radio7Days) {
            thresholdDays = 7;
        } else if (checkedThresholdId == R.id.radio5Days) {
            thresholdDays = 5;
        } else {
            thresholdDays = 3;
        }

        String unitSystem = "metric";
        int checkedUnitId = radioGroupUnits.getCheckedRadioButtonId();
        if (checkedUnitId == R.id.radioImperial) {
            unitSystem = "imperial";
        } else {
            unitSystem = "metric";
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_EXPIRY_ALERTS, alertsEnabled);
        editor.putInt(KEY_EXPIRY_THRESHOLD, thresholdDays);
        editor.putString(KEY_UNIT_SYSTEM, unitSystem);
        editor.apply();

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
