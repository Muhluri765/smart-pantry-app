package com.richfield.smartpantryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.richfield.smartpantryapp.adapter.PantryAdapter;
import com.richfield.smartpantryapp.database.DatabaseHelper;
import com.richfield.smartpantryapp.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Primary dashboard Activity displaying the user's pantry inventory.
 * Binds SQLite database records to a RecyclerView, hosts Toolbar navigation,
 * handles deletion confirmation, and coordinates navigation across app screens.
 */
public class PantryListActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemListener {

    private RecyclerView recyclerViewPantry;
    private TextView textEmptyPantry;
    private FloatingActionButton fabAddIngredient;
    private PantryAdapter pantryAdapter;
    private DatabaseHelper databaseHelper;
    private List<PantryItem> pantryItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        initToolbar();
        initViews();
        initDatabase();
        initRecyclerView();
        initFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initViews() {
        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);
        fabAddIngredient = findViewById(R.id.fabAddIngredient);
    }

    private void initDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    private void initRecyclerView() {
        pantryItemList = new ArrayList<>();
        pantryAdapter = new PantryAdapter(pantryItemList, this);
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void initFab() {
        fabAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadPantryItems() {
        pantryItemList = databaseHelper.getAllPantryItems();
        pantryAdapter.updateList(pantryItemList);

        if (pantryItemList.isEmpty()) {
            textEmptyPantry.setVisibility(View.VISIBLE);
            recyclerViewPantry.setVisibility(View.GONE);
        } else {
            textEmptyPantry.setVisibility(View.GONE);
            recyclerViewPantry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pantry_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_suggested_recipes) {
            Intent intent = new Intent(PantryListActivity.this, SuggestedRecipesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(PantryListActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
        intent.putExtra("pantry_item_id", item.getId());
        startActivity(intent);
    }

    @Override
    public void onItemDelete(PantryItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\" from your pantry?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleted = databaseHelper.deletePantryItem(item.getId());
                        if (deleted) {
                            Toast.makeText(PantryListActivity.this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                            loadPantryItems();
                        } else {
                            Toast.makeText(PantryListActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
