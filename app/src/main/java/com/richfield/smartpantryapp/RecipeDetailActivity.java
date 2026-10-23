package com.richfield.smartpantryapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.richfield.smartpantryapp.database.DatabaseHelper;
import com.richfield.smartpantryapp.model.Recipe;
import com.richfield.smartpantryapp.model.RecipeIngredient;

import java.util.List;
import java.util.Locale;

/**
 * Screen 4: Recipe Detail Activity.
 * Retrieves a recipe by ID from SQLite, renders the recipe name,
 * formats all required ingredients with quantities and units,
 * and displays preparation instructions.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    private Toolbar toolbarRecipeDetail;
    private TextView textDetailRecipeName;
    private TextView textDetailIngredients;
    private TextView textDetailInstructions;

    private DatabaseHelper databaseHelper;
    private long recipeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        loadRecipeData();
    }

    private void initViews() {
        toolbarRecipeDetail = findViewById(R.id.toolbarRecipeDetail);
        textDetailRecipeName = findViewById(R.id.textDetailRecipeName);
        textDetailIngredients = findViewById(R.id.textDetailIngredients);
        textDetailInstructions = findViewById(R.id.textDetailInstructions);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarRecipeDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recipe Details");
        }
    }

    private void loadRecipeData() {
        if (getIntent().hasExtra("recipe_id")) {
            recipeId = getIntent().getLongExtra("recipe_id", -1);
        }

        if (recipeId == -1) {
            Toast.makeText(this, "Error loading recipe details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Recipe recipe = databaseHelper.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Render recipe name
        textDetailRecipeName.setText(recipe.getName());

        // Format required ingredients list
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            StringBuilder ingredientsBuilder = new StringBuilder();
            for (int i = 0; i < ingredients.size(); i++) {
                RecipeIngredient item = ingredients.get(i);
                ingredientsBuilder.append("• ")
                        .append(formatQuantity(item.getQuantityRequired()))
                        .append(" ")
                        .append(item.getUnit())
                        .append(" ")
                        .append(item.getIngredientName());

                if (i < ingredients.size() - 1) {
                    ingredientsBuilder.append("\n");
                }
            }
            textDetailIngredients.setText(ingredientsBuilder.toString());
        } else {
            textDetailIngredients.setText("No specific ingredients listed.");
        }

        // Render cooking instructions
        if (recipe.getInstructions() != null && !recipe.getInstructions().trim().isEmpty()) {
            textDetailInstructions.setText(recipe.getInstructions());
        } else {
            textDetailInstructions.setText("No preparation instructions provided.");
        }
    }

    private String formatQuantity(double quantity) {
        if (quantity == (long) quantity) {
            return String.format(Locale.US, "%d", (long) quantity);
        } else {
            return String.format(Locale.US, "%s", quantity);
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
