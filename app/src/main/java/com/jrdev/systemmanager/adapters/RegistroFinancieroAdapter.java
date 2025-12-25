package com.jrdev.systemmanager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jrdev.systemmanager.R;
import com.jrdev.systemmanager.models.TablaRegistroItem;

import java.util.LinkedList;
import java.util.List;

public class RegistroFinancieroAdapter extends RecyclerView.Adapter<RegistroFinancieroAdapter.ViewHolder> {
    private List<TablaRegistroItem> registros = new LinkedList<>();

    @NonNull
    @Override
    public RegistroFinancieroAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fila_tablaregistro, parent, false);
        return new RegistroFinancieroAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RegistroFinancieroAdapter.ViewHolder holder, int position) {
        TablaRegistroItem item = registros.get(position);

        holder.tvNumApto.setText(item.numApto);
        holder.tvCuotaMensual.setText(String.format("DOP$ %.2f", item.cuotaMensual));
        holder.tvDescripcion.setText(item.descripcion);
        holder.tvMontoPagar.setText(String.format("DOP$ %.2f", item.montoPagar));
        holder.tvBalance.setText(String.format("DOP$ %.2f", item.balance));

        if (item.balance >= 0) {
            holder.tvBalance.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.tvBalance.setTextColor(Color.parseColor("#FF6961")); // Rojo
        }
    }

    @Override
    public int getItemCount() { return registros.size(); }

    public void setItems(List<TablaRegistroItem> items) {
        this.registros = items != null ? items : new LinkedList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumApto, tvCuotaMensual, tvDescripcion, tvMontoPagar, tvBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumApto = itemView.findViewById(R.id.tvNumApto);
            tvCuotaMensual = itemView.findViewById(R.id.tvCuotaMensual);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvMontoPagar = itemView.findViewById(R.id.tvMontoPagar);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}
