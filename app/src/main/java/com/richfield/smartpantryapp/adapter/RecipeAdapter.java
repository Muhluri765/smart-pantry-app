package com.richfield.smartpantryapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantryapp.R;
import com.richfield.smartpantryapp.model.Recipe;
import com.richfield.smartpantryapp.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView Adapter for rendering suggested recipes.
 * Connects qualifying recipes to item_recipe.xml layouts and
 * routes item selection clicks to the host activity.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipeList;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.recipeList = (recipeList != null) ? recipeList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.textRecipeName.setText(recipe.getName());

        // Construct a clean ingredients summary string
        StringBuilder summary = new StringBuilder("Requires: ");
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            for (int i = 0; i < ingredients.size(); i++) {
                summary.append(ingredients.get(i).getIngredientName());
                if (i < ingredients.size() - 1) {
                    summary.append(", ");
                }
            }
        } else {
            summary.append("See recipe instructions");
        }
        holder.textRecipeIngredientSummary.setText(summary.toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList != null ? recipeList.size() : 0;
    }

    public void updateList(List<Recipe> newList) {
        this.recipeList = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public final TextView textRecipeName;
        public final TextView textRecipeIngredientSummary;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textRecipeIngredientSummary = itemView.findViewById(R.id.textRecipeIngredientSummary);
        }
    }
}
