package com.richfield.smartpantryapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.richfield.smartpantryapp.database.DatabaseHelper;
import com.richfield.smartpantryapp.model.PantryItem;

import java.util.Calendar;
import java.util.Locale;

/**
 * Screen 2: Add / Edit Ingredient Activity.
 * Captures user input with strict validation, handles date selection via DatePickerDialog,
 * and performs SQLite insert or update operations based on intent extras.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"unit", "g", "kg", "ml", "l", "slice", "clove", "pinch"};

    private Toolbar toolbarAddEdit;
    private EditText editIngredientName;
    private EditText editIngredientQuantity;
    private Spinner spinnerIngredientUnit;
    private EditText editIngredientExpiry;
    private Button btnSaveIngredient;

    private DatabaseHelper databaseHelper;
    private long existingItemId = -1;
    private PantryItem existingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupUnitSpinner();
        setupDatePicker();
        checkIntentData();
        setupSaveButton();
    }

    private void initViews() {
        toolbarAddEdit = findViewById(R.id.toolbarAddEdit);
        editIngredientName = findViewById(R.id.editIngredientName);
        editIngredientQuantity = findViewById(R.id.editIngredientQuantity);
        spinnerIngredientUnit = findViewById(R.id.spinnerIngredientUnit);
        editIngredientExpiry = findViewById(R.id.editIngredientExpiry);
        btnSaveIngredient = findViewById(R.id.btnSaveIngredient);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarAddEdit);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupUnitSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                UNITS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIngredientUnit.setAdapter(adapter);
    }

    private void setupDatePicker() {
        editIngredientExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AddEditIngredientActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String formattedDate = String.format(
                                        Locale.US,
                                        "%04d-%02d-%02d",
                                        selectedYear,
                                        selectedMonth + 1,
                                        selectedDay
                                );
                                editIngredientExpiry.setText(formattedDate);
                            }
                        },
                        year,
                        month,
                        day
                );
                datePickerDialog.show();
            }
        });
    }

    private void checkIntentData() {
        if (getIntent().hasExtra("pantry_item_id")) {
            existingItemId = getIntent().getLongExtra("pantry_item_id", -1);
            if (existingItemId != -1) {
                existingItem = databaseHelper.getPantryItem(existingItemId);
                if (existingItem != null) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Edit Ingredient");
                    }
                    btnSaveIngredient.setText("Update Ingredient");

                    editIngredientName.setText(existingItem.getName());
                    if (existingItem.getQuantity() == (long) existingItem.getQuantity()) {
                        editIngredientQuantity.setText(String.format(Locale.US, "%d", (long) existingItem.getQuantity()));
                    } else {
                        editIngredientQuantity.setText(String.format(Locale.US, "%s", existingItem.getQuantity()));
                    }

                    setSpinnerSelection(existingItem.getUnit());

                    if (existingItem.getExpiryDate() != null) {
                        editIngredientExpiry.setText(existingItem.getExpiryDate());
                    }
                }
            }
        }
    }

    private void setSpinnerSelection(String unit) {
        if (unit == null) return;
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(unit.trim())) {
                spinnerIngredientUnit.setSelection(i);
                break;
            }
        }
    }

    private void setupSaveButton() {
        btnSaveIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIngredient();
            }
        });
    }

    private void saveIngredient() {
        String name = editIngredientName.getText().toString().trim();
        String quantityStr = editIngredientQuantity.getText().toString().trim();
        String unit = spinnerIngredientUnit.getSelectedItem().toString();
        String expiry = editIngredientExpiry.getText().toString().trim();

        // Form Validation
        if (TextUtils.isEmpty(name)) {
            editIngredientName.setError("Ingredient name is required");
            editIngredientName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(quantityStr)) {
            editIngredientQuantity.setError("Quantity is required");
            editIngredientQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                editIngredientQuantity.setError("Quantity must be greater than zero");
                editIngredientQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editIngredientQuantity.setError("Please enter a valid number");
            editIngredientQuantity.requestFocus();
            return;
        }

        // Database Persistence
        if (existingItemId == -1 || existingItem == null) {
            PantryItem newItem = new PantryItem(name, quantity, unit, expiry);
            long insertedId = databaseHelper.insertPantryItem(newItem);
            if (insertedId != -1) {
                Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save ingredient", Toast.LENGTH_SHORT).show();
            }
        } else {
            existingItem.setName(name);
            existingItem.setQuantity(quantity);
            existingItem.setUnit(unit);
            existingItem.setExpiryDate(expiry);

            boolean updated = databaseHelper.updatePantryItem(existingItem);
            if (updated) {
                Toast.makeText(this, name + " updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update ingredient", Toast.LENGTH_SHORT).show();
            }
        }
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
