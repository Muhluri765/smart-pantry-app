package com.richfield.smartpantryapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantryapp.R;
import com.richfield.smartpantryapp.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView Adapter for rendering pantry ingredients.
 * Connects the pantry dataset with item_pantry.xml layouts and
 * exposes callbacks for selecting and deleting items.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnPantryItemListener {
        void onItemClick(PantryItem item);
        void onItemDelete(PantryItem item, int position);
    }

    private List<PantryItem> pantryItemList;
    private final OnPantryItemListener listener;

    public PantryAdapter(List<PantryItem> pantryItemList, OnPantryItemListener listener) {
        this.pantryItemList = (pantryItemList != null) ? pantryItemList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryItemList.get(position);

        holder.textName.setText(item.getName());

        String quantityText = "Quantity: " + formatQuantity(item.getQuantity()) + " " + item.getUnit();
        holder.textQuantity.setText(quantityText);

        if (item.getExpiryDate() != null && !item.getExpiryDate().trim().isEmpty()) {
            holder.textExpiry.setText("Expires: " + item.getExpiryDate());
        } else {
            holder.textExpiry.setText("Expires: Not set");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemDelete(item, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return pantryItemList != null ? pantryItemList.size() : 0;
    }

    public void updateList(List<PantryItem> newList) {
        this.pantryItemList = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String formatQuantity(double quantity) {
        if (quantity == (long) quantity) {
            return String.format("%d", (long) quantity);
        } else {
            return String.format("%s", quantity);
        }
    }

    public static class PantryViewHolder extends RecyclerView.ViewHolder {
        public final TextView textName;
        public final TextView textQuantity;
        public final TextView textExpiry;
        public final ImageButton btnDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textPantryItemName);
            textQuantity = itemView.findViewById(R.id.textPantryItemQuantity);
            textExpiry = itemView.findViewById(R.id.textPantryItemExpiry);
            btnDelete = itemView.findViewById(R.id.btnDeletePantryItem);
        }
    }
}
