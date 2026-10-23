package com.richfield.smartpantryapp;

import android.content.Intent;
import android.os.Bundle;
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
 * Queries current pantry inventory from SQLite, runs strict recipe matching,
 * and displays qualifying recipes in a RecyclerView.
 * Shows an informative empty-state message if zero recipes match.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private Toolbar toolbarSuggested;
    private RecyclerView recyclerViewSuggested;
    private TextView textNoRecipes;
    private RecipeAdapter recipeAdapter;
    private DatabaseHelper databaseHelper;
    private List<Recipe> matchingRecipes;

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
        loadMatchingRecipes();
    }

    private void initViews() {
        toolbarSuggested = findViewById(R.id.toolbarSuggested);
        recyclerViewSuggested = findViewById(R.id.recyclerViewSuggested);
        textNoRecipes = findViewById(R.id.textNoRecipes);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarSuggested);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Suggested Recipes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        matchingRecipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(matchingRecipes, this);
        recyclerViewSuggested.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSuggested.setAdapter(recipeAdapter);
    }

    private void loadMatchingRecipes() {
        List<PantryItem> pantryItems = databaseHelper.getAllPantryItems();
        List<Recipe> allRecipes = databaseHelper.getAllRecipes();

        // Execute strict-matching logic
        matchingRecipes = RecipeMatcher.getStrictMatchingRecipes(allRecipes, pantryItems);
        recipeAdapter.updateList(matchingRecipes);

        if (matchingRecipes.isEmpty()) {
            textNoRecipes.setVisibility(View.VISIBLE);
            recyclerViewSuggested.setVisibility(View.GONE);
        } else {
            textNoRecipes.setVisibility(View.GONE);
            recyclerViewSuggested.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
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
