package com.jrdev.systemmanager.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.jrdev.systemmanager.DataBaseConnection.dao.PropietarioDao;
import com.jrdev.systemmanager.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PropietariosAdapter extends RecyclerView.Adapter<PropietariosAdapter.ViewHolder> {

    private List<PropietarioDao> propietarios = new LinkedList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fila_tabla, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PropietarioDao propietario = propietarios.get(position);
        // Estado
        if ("Verde".equalsIgnoreCase(propietario.estado)) {
            holder.tvEstado.setText("Al día");
            holder.cardEstado.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Verde
        } else if ("Rojo".equalsIgnoreCase(propietario.estado)) {
            holder.tvEstado.setText("Pendiente");
            holder.cardEstado.setCardBackgroundColor(Color.parseColor("#FF6961")); // Rojo
        }

        // Apartamento
        holder.tvApto.setText(propietario.numApto != null ? propietario.numApto : "");

        // Nombre
        holder.tvNombre.setText(propietario.nombrePropietario != null ? propietario.nombrePropietario : "");

        // Total Abonado
        holder.tvAbonado.setText(String.format("DOP$ %.2f", propietario.totalabonado != null ? propietario.totalabonado : 0.0f));

        // Balance (Deuda)
        float balance = propietario.balance != null ? propietario.balance : 0.0f;
        holder.tvBalance.setText(String.format("DOP$ %.2f", balance));

        // Color del balance según sea positivo o negativo
        if (balance >= 0) {
            holder.tvBalance.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.tvBalance.setTextColor(Color.parseColor("#FF6961")); // Rojo
        }
    }

    @Override
    public int getItemCount() {
        return propietarios.size();
    }

    public void actualizarLista(List<PropietarioDao> nuevaLista) {
        this.propietarios = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardEstado;
        TextView tvEstado, tvApto, tvNombre, tvAbonado, tvBalance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardEstado = itemView.findViewById(R.id.cardEstado);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvApto = itemView.findViewById(R.id.tvApto);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvAbonado = itemView.findViewById(R.id.tvAbonado);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}