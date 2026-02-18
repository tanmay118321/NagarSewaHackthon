package com.deepdefender.nagarsewahackthon.Admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deepdefender.nagarsewahackthon.R;

import java.util.List;

public class AssignedComplaintAdapter
        extends RecyclerView.Adapter<AssignedComplaintAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(Complaint complaint);
    }

    private List<Complaint> list;
    private OnItemClick listener;

    public AssignedComplaintAdapter(List<Complaint> list, OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assigned_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Complaint item = list.get(position);

        // Title → Issues
        holder.title.setText(item.getIssues());

        // ID/Sub → Address
        holder.id.setText(item.getAddress());

        // Hide divider for last item
        if (position == list.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        // Click → open detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 🔽 ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, id;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.assignTitle);
            id = itemView.findViewById(R.id.assignId);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
