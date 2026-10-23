package com.richfield.smartpantryapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.richfield.smartpantryapp.model.PantryItem;
import com.richfield.smartpantryapp.model.Recipe;
import com.richfield.smartpantryapp.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper managing the local database "pantry_manager.db".
 * Handles full CRUD operations for pantry items, recipe data retrieval,
 * and automatic seeding of default recipes upon initial installation.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pantry_manager.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_PANTRY = "pantry_item";
    public static final String TABLE_RECIPE = "recipe";
    public static final String TABLE_RECIPE_INGREDIENT = "recipe_ingredient";

    // Pantry Item columns
    public static final String COLUMN_PANTRY_ID = "id";
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QUANTITY = "quantity";
    public static final String COLUMN_PANTRY_UNIT = "unit";
    public static final String COLUMN_PANTRY_EXPIRY = "expiry_date";

    // Recipe columns
    public static final String COLUMN_RECIPE_ID = "id";
    public static final String COLUMN_RECIPE_NAME = "name";
    public static final String COLUMN_RECIPE_INSTRUCTIONS = "instructions";

    // Recipe Ingredient columns
    public static final String COLUMN_RI_ID = "id";
    public static final String COLUMN_RI_RECIPE_ID = "recipe_id";
    public static final String COLUMN_RI_NAME = "ingredient_name";
    public static final String COLUMN_RI_QUANTITY = "quantity_required";
    public static final String COLUMN_RI_UNIT = "unit";

    // Create table statements
    private static final String CREATE_TABLE_PANTRY = "CREATE TABLE " + TABLE_PANTRY + " ("
            + COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PANTRY_NAME + " TEXT NOT NULL, "
            + COLUMN_PANTRY_QUANTITY + " REAL NOT NULL, "
            + COLUMN_PANTRY_UNIT + " TEXT NOT NULL, "
            + COLUMN_PANTRY_EXPIRY + " TEXT"
            + ");";

    private static final String CREATE_TABLE_RECIPE = "CREATE TABLE " + TABLE_RECIPE + " ("
            + COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_RECIPE_NAME + " TEXT NOT NULL, "
            + COLUMN_RECIPE_INSTRUCTIONS + " TEXT NOT NULL"
            + ");";

    private static final String CREATE_TABLE_RECIPE_INGREDIENT = "CREATE TABLE " + TABLE_RECIPE_INGREDIENT + " ("
            + COLUMN_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_RI_RECIPE_ID + " INTEGER NOT NULL, "
            + COLUMN_RI_NAME + " TEXT NOT NULL, "
            + COLUMN_RI_QUANTITY + " REAL NOT NULL, "
            + COLUMN_RI_UNIT + " TEXT NOT NULL, "
            + "FOREIGN KEY (" + COLUMN_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPE + "(" + COLUMN_RECIPE_ID + ") ON DELETE CASCADE"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPE);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENT);
        seedDefaultRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ==========================================
    // Pantry CRUD Operations
    // ==========================================

    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_NAME, item.getName().trim());
        values.put(COLUMN_PANTRY_QUANTITY, item.getQuantity());
        values.put(COLUMN_PANTRY_UNIT, item.getUnit().trim());
        values.put(COLUMN_PANTRY_EXPIRY, item.getExpiryDate());
        long id = db.insert(TABLE_PANTRY, null, values);
        item.setId(id);
        return id;
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_PANTRY + " ORDER BY " + COLUMN_PANTRY_NAME + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                PantryItem item = new PantryItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_UNIT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_EXPIRY))
                );
                list.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        PantryItem item = null;
        Cursor cursor = db.query(TABLE_PANTRY, null, COLUMN_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            item = new PantryItem(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_UNIT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_EXPIRY))
            );
            cursor.close();
        }
        return item;
    }

    public boolean updatePantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_NAME, item.getName().trim());
        values.put(COLUMN_PANTRY_QUANTITY, item.getQuantity());
        values.put(COLUMN_PANTRY_UNIT, item.getUnit().trim());
        values.put(COLUMN_PANTRY_EXPIRY, item.getExpiryDate());

        int rows = db.update(TABLE_PANTRY, values, COLUMN_PANTRY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
        return rows > 0;
    }

    public boolean deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COLUMN_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // ==========================================
    // Recipe & Recipe Ingredients Operations
    // ==========================================

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_RECIPE + " ORDER BY " + COLUMN_RECIPE_NAME + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INSTRUCTIONS));

                List<RecipeIngredient> ingredients = getIngredientsForRecipe(recipeId);
                Recipe recipe = new Recipe(recipeId, name, instructions, ingredients);
                recipeList.add(recipe);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return recipeList;
    }

    public Recipe getRecipe(long recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Recipe recipe = null;
        Cursor cursor = db.query(TABLE_RECIPE, null, COLUMN_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
            String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INSTRUCTIONS));
            List<RecipeIngredient> ingredients = getIngredientsForRecipe(recipeId);
            recipe = new Recipe(recipeId, name, instructions, ingredients);
            cursor.close();
        }
        return recipe;
    }

    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENT, null, COLUMN_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                RecipeIngredient ingredient = new RecipeIngredient(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RI_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RI_RECIPE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RI_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RI_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RI_UNIT))
                );
                list.add(ingredient);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // ==========================================
    // Seed 16 Default Recipes (15-20 Requirement)
    // ==========================================

    private void seedDefaultRecipes(SQLiteDatabase db) {
        long rId;

        // 1. Scrambled Eggs
        rId = addRecipeEntry(db, "Scrambled Eggs", "Whisk eggs with milk and salt. Melt butter in a skillet over medium heat, pour in egg mixture, and stir gently until soft curds form.");
        addIngredientEntry(db, rId, "Egg", 2.0, "unit");
        addIngredientEntry(db, rId, "Butter", 15.0, "g");
        addIngredientEntry(db, rId, "Milk", 50.0, "ml");
        addIngredientEntry(db, rId, "Salt", 1.0, "pinch");

        // 2. Classic Pancakes
        rId = addRecipeEntry(db, "Classic Pancakes", "Whisk flour and sugar with milk, egg, and melted butter into a smooth batter. Cook spoonfuls on a greased pan until golden on both sides.");
        addIngredientEntry(db, rId, "Flour", 150.0, "g");
        addIngredientEntry(db, rId, "Milk", 250.0, "ml");
        addIngredientEntry(db, rId, "Egg", 1.0, "unit");
        addIngredientEntry(db, rId, "Sugar", 25.0, "g");
        addIngredientEntry(db, rId, "Butter", 20.0, "g");

        // 3. Grilled Cheese Sandwich
        rId = addRecipeEntry(db, "Grilled Cheese Sandwich", "Butter one side of each bread slice. Place cheese between the unbuttered sides and grill in a skillet until bread is toasted and cheese is melted.");
        addIngredientEntry(db, rId, "Bread", 2.0, "slice");
        addIngredientEntry(db, rId, "Cheese", 50.0, "g");
        addIngredientEntry(db, rId, "Butter", 15.0, "g");

        // 4. Tomato Garlic Pasta
        rId = addRecipeEntry(db, "Tomato Garlic Pasta", "Boil pasta until al dente. Sauté minced garlic and diced tomatoes in olive oil, then toss with cooked pasta.");
        addIngredientEntry(db, rId, "Pasta", 200.0, "g");
        addIngredientEntry(db, rId, "Tomato", 2.0, "unit");
        addIngredientEntry(db, rId, "Garlic", 2.0, "clove");
        addIngredientEntry(db, rId, "Olive Oil", 15.0, "ml");

        // 5. French Toast
        rId = addRecipeEntry(db, "French Toast", "Whisk egg and milk in a bowl. Dip bread slices into mixture. Fry in melted butter over medium heat until golden brown on both sides.");
        addIngredientEntry(db, rId, "Bread", 2.0, "slice");
        addIngredientEntry(db, rId, "Egg", 1.0, "unit");
        addIngredientEntry(db, rId, "Milk", 50.0, "ml");
        addIngredientEntry(db, rId, "Butter", 15.0, "g");

        // 6. Egg Fried Rice
        rId = addRecipeEntry(db, "Egg Fried Rice", "Heat oil in a wok. Fry chopped onion, scramble in eggs, add cooked rice, and season thoroughly with soy sauce.");
        addIngredientEntry(db, rId, "Rice", 200.0, "g");
        addIngredientEntry(db, rId, "Egg", 2.0, "unit");
        addIngredientEntry(db, rId, "Onion", 1.0, "unit");
        addIngredientEntry(db, rId, "Soy Sauce", 15.0, "ml");
        addIngredientEntry(db, rId, "Oil", 15.0, "ml");

        // 7. Garlic Butter Noodles
        rId = addRecipeEntry(db, "Garlic Butter Noodles", "Cook pasta in salted water. In a pan, melt butter and gently cook minced garlic. Toss hot pasta in garlic butter and top with cheese.");
        addIngredientEntry(db, rId, "Pasta", 200.0, "g");
        addIngredientEntry(db, rId, "Butter", 30.0, "g");
        addIngredientEntry(db, rId, "Garlic", 3.0, "clove");
        addIngredientEntry(db, rId, "Cheese", 30.0, "g");

        // 8. Cheese Omelette
        rId = addRecipeEntry(db, "Cheese Omelette", "Beat eggs. Melt butter in a non-stick pan, pour in eggs, cook until nearly set, add cheese across one half, and fold over.");
        addIngredientEntry(db, rId, "Egg", 2.0, "unit");
        addIngredientEntry(db, rId, "Cheese", 40.0, "g");
        addIngredientEntry(db, rId, "Butter", 15.0, "g");

        // 9. Garlic Bread
        rId = addRecipeEntry(db, "Garlic Bread", "Mix softened butter with minced garlic. Spread across bread slices and toast or bake until crispy and golden.");
        addIngredientEntry(db, rId, "Bread", 2.0, "slice");
        addIngredientEntry(db, rId, "Butter", 20.0, "g");
        addIngredientEntry(db, rId, "Garlic", 2.0, "clove");

        // 10. Mashed Potatoes
        rId = addRecipeEntry(db, "Mashed Potatoes", "Boil peeled potatoes until fork-tender. Drain and mash thoroughly with butter, warm milk, and salt until smooth.");
        addIngredientEntry(db, rId, "Potato", 3.0, "unit");
        addIngredientEntry(db, rId, "Butter", 30.0, "g");
        addIngredientEntry(db, rId, "Milk", 60.0, "ml");
        addIngredientEntry(db, rId, "Salt", 1.0, "pinch");

        // 11. Chicken Stir Fry
        rId = addRecipeEntry(db, "Chicken Stir Fry", "Slice chicken and onion. Stir-fry chicken in hot oil until cooked through, add onions, and toss with soy sauce.");
        addIngredientEntry(db, rId, "Chicken", 250.0, "g");
        addIngredientEntry(db, rId, "Onion", 1.0, "unit");
        addIngredientEntry(db, rId, "Soy Sauce", 20.0, "ml");
        addIngredientEntry(db, rId, "Oil", 15.0, "ml");

        // 12. Tomato Onion Salad
        rId = addRecipeEntry(db, "Tomato Onion Salad", "Thinly slice tomatoes and onion. Arrange on a plate, drizzle with olive oil, and season with salt.");
        addIngredientEntry(db, rId, "Tomato", 2.0, "unit");
        addIngredientEntry(db, rId, "Onion", 1.0, "unit");
        addIngredientEntry(db, rId, "Olive Oil", 15.0, "ml");
        addIngredientEntry(db, rId, "Salt", 1.0, "pinch");

        // 13. Oatmeal Porridge
        rId = addRecipeEntry(db, "Oatmeal Porridge", "Combine oats and milk in a pot. Simmer gently over medium heat for 5 minutes, stirring frequently, and sweeten with sugar.");
        addIngredientEntry(db, rId, "Oats", 50.0, "g");
        addIngredientEntry(db, rId, "Milk", 200.0, "ml");
        addIngredientEntry(db, rId, "Sugar", 15.0, "g");

        // 14. Butter Rice
        rId = addRecipeEntry(db, "Butter Rice", "Cook rice until tender. While steaming hot, stir in butter and salt until melted and evenly distributed.");
        addIngredientEntry(db, rId, "Rice", 200.0, "g");
        addIngredientEntry(db, rId, "Butter", 25.0, "g");
        addIngredientEntry(db, rId, "Salt", 1.0, "pinch");

        // 15. Soy Garlic Chicken
        rId = addRecipeEntry(db, "Soy Garlic Chicken", "Cut chicken into bite-sized pieces. Sauté in oil with minced garlic until browned, then glaze with soy sauce.");
        addIngredientEntry(db, rId, "Chicken", 250.0, "g");
        addIngredientEntry(db, rId, "Garlic", 3.0, "clove");
        addIngredientEntry(db, rId, "Soy Sauce", 30.0, "ml");
        addIngredientEntry(db, rId, "Oil", 15.0, "ml");

        // 16. Simple Quesadilla
        rId = addRecipeEntry(db, "Simple Quesadilla", "Place shredded cheese between two tortillas. Melt butter in a skillet and cook tortilla on medium heat until golden and cheese is melted.");
        addIngredientEntry(db, rId, "Tortilla", 2.0, "unit");
        addIngredientEntry(db, rId, "Cheese", 60.0, "g");
        addIngredientEntry(db, rId, "Butter", 10.0, "g");
    }

    private long addRecipeEntry(SQLiteDatabase db, String name, String instructions) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPE_NAME, name);
        values.put(COLUMN_RECIPE_INSTRUCTIONS, instructions);
        return db.insert(TABLE_RECIPE, null, values);
    }

    private void addIngredientEntry(SQLiteDatabase db, long recipeId, String name, double quantity, String unit) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_RI_RECIPE_ID, recipeId);
        values.put(COLUMN_RI_NAME, name);
        values.put(COLUMN_RI_QUANTITY, quantity);
        values.put(COLUMN_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENT, null, values);
    }
}
