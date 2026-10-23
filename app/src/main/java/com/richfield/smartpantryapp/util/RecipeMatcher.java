package com.richfield.smartpantryapp.util;

import com.richfield.smartpantryapp.model.PantryItem;
import com.richfield.smartpantryapp.model.Recipe;
import com.richfield.smartpantryapp.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility executing the strict recipe matching rule.
 * Ensures a recipe is suggested only if 100% of its required ingredients
 * are present in the pantry in sufficient quantities, handling singular/plural
 * string variations and unit normalization.
 */
public class RecipeMatcher {

    /**
     * Filters a list of recipes, returning only those that can be fully cooked
     * with the available pantry items.
     *
     * @param allRecipes  Full collection of recipes from the database.
     * @param pantryItems Current available inventory in the pantry.
     * @return List of recipes meeting the strict-matching criteria.
     */
    public static List<Recipe> getStrictMatchingRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        List<Recipe> matchingRecipes = new ArrayList<>();
        if (allRecipes == null || pantryItems == null) {
            return matchingRecipes;
        }

        for (Recipe recipe : allRecipes) {
            if (canMakeRecipe(recipe, pantryItems)) {
                matchingRecipes.add(recipe);
            }
        }
        return matchingRecipes;
    }

    /**
     * Determines whether all ingredients for a given recipe are satisfied by
     * items currently in the pantry.
     *
     * @param recipe      The recipe to evaluate.
     * @param pantryItems Current pantry items.
     * @return true if every ingredient is present in required quantity; false otherwise.
     */
    public static boolean canMakeRecipe(Recipe recipe, List<PantryItem> pantryItems) {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return false;
        }

        for (RecipeIngredient required : recipe.getIngredients()) {
            double totalQuantityFound = 0.0;
            String normalizedRequiredName = normalizeName(required.getIngredientName());
            String normalizedRequiredUnit = normalizeUnit(required.getUnit());

            for (PantryItem item : pantryItems) {
                if (item == null || item.getName() == null) {
                    continue;
                }

                String normalizedPantryName = normalizeName(item.getName());
                String normalizedPantryUnit = normalizeUnit(item.getUnit());

                // Match ingredient name
                if (normalizedRequiredName.equals(normalizedPantryName)) {
                    // Match unit or allow compatible unit definitions
                    if (areUnitsCompatible(normalizedRequiredUnit, normalizedPantryUnit)) {
                        totalQuantityFound += item.getQuantity();
                    }
                }
            }

            // Strict rule: if any single ingredient quantity is insufficient, disqualify recipe
            if (totalQuantityFound < required.getQuantityRequired()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Normalizes ingredient names to handle singular/plural variations and casing.
     * e.g., "Tomatoes" -> "tomato", "Eggs" -> "egg", "potatoes" -> "potato".
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        String cleaned = name.trim().toLowerCase();

        // Irregular or common cooking plurals ending in "es"
        if (cleaned.endsWith("tomatoes")) {
            return "tomato";
        }
        if (cleaned.endsWith("potatoes")) {
            return "potato";
        }
        if (cleaned.endsWith("berries")) {
            return cleaned.substring(0, cleaned.length() - 3) + "y";
        }

        // Standard plural stripping
        if (cleaned.endsWith("es") && cleaned.length() > 3) {
            return cleaned.substring(0, cleaned.length() - 2);
        }
        if (cleaned.endsWith("s") && !cleaned.endsWith("ss") && cleaned.length() > 2) {
            return cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }

    /**
     * Normalizes unit strings to consistent standard abbreviations.
     */
    public static String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }

        String cleaned = unit.trim().toLowerCase();

        if (cleaned.equals("g") || cleaned.equals("gram") || cleaned.equals("grams")) {
            return "g";
        }
        if (cleaned.equals("kg") || cleaned.equals("kilogram") || cleaned.equals("kilograms")) {
            return "kg";
        }
        if (cleaned.equals("ml") || cleaned.equals("milliliter") || cleaned.equals("milliliters")) {
            return "ml";
        }
        if (cleaned.equals("l") || cleaned.equals("liter") || cleaned.equals("liters")) {
            return "l";
        }
        if (cleaned.equals("slice") || cleaned.equals("slices")) {
            return "slice";
        }
        if (cleaned.equals("unit") || cleaned.equals("units") || cleaned.equals("item") || cleaned.equals("items")) {
            return "unit";
        }
        if (cleaned.equals("clove") || cleaned.equals("cloves")) {
            return "clove";
        }
        if (cleaned.equals("pinch") || cleaned.equals("pinches")) {
            return "pinch";
        }

        return cleaned;
    }

    /**
     * Checks if units match directly or can be reconciled.
     */
    private static boolean areUnitsCompatible(String requiredUnit, String pantryUnit) {
        if (requiredUnit.isEmpty() || pantryUnit.isEmpty()) {
            return true;
        }
        return requiredUnit.equals(pantryUnit);
    }
}
