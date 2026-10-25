package com.richfield.smartpantryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantryapp.adapter.RecipeAdapter;
import com.richfield.smartpantryapp.database.DatabaseHelper;
import com.richfield.smartpantryapp.model.PantryItem;
import com.richfield.smartpantryapp.model.Recipe;
import com.richfield.smartpantryapp.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3: Suggested Recipes Activity.
 * Displays recipes matching current pantry inventory or the full recipe collection
 * via a clean Toolbar Options Menu toggle.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private Toolbar toolbarSuggested;
    private RecyclerView recyclerViewSuggested;
    private TextView textNoRecipes;
    private RecipeAdapter recipeAdapter;
    private DatabaseHelper databaseHelper;
    private List<Recipe> displayedRecipes;
    private boolean showAllRecipes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void initViews() {
        toolbarSuggested = findViewById(R.id.toolbarSuggested);
        recyclerViewSuggested = findViewById(R.id.recyclerViewSuggested);
        textNoRecipes = findViewById(R.id.textNoRecipes);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarSuggested);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(showAllRecipes ? "All Recipes" : "Suggested Recipes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        displayedRecipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(displayedRecipes, this);
        recyclerViewSuggested.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSuggested.setAdapter(recipeAdapter);
    }

    private void loadRecipes() {
        if (showAllRecipes) {
            displayedRecipes = databaseHelper.getAllRecipes();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("All Recipes (" + displayedRecipes.size() + ")");
            }
        } else {
            List<PantryItem> pantryItems = databaseHelper.getAllPantryItems();
            List<Recipe> allRecipes = databaseHelper.getAllRecipes();
            displayedRecipes = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Suggested Recipes");
            }
        }

        recipeAdapter.updateList(displayedRecipes);

        if (displayedRecipes.isEmpty()) {
            if (showAllRecipes) {
                textNoRecipes.setText("No recipes found in database");
            } else {
                textNoRecipes.setText("No recipes match your pantry yet - add more ingredients");
            }
            textNoRecipes.setVisibility(View.VISIBLE);
            recyclerViewSuggested.setVisibility(View.GONE);
        } else {
            textNoRecipes.setVisibility(View.GONE);
            recyclerViewSuggested.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_suggested_recipes, menu);
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_all_recipes);
        if (toggleItem != null) {
            toggleItem.setTitle(showAllRecipes ? "Show Matched" : "Show All Recipes");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_toggle_all_recipes) {
            showAllRecipes = !showAllRecipes;
            invalidateOptionsMenu();
            loadRecipes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
    }
}
