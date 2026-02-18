package com.deepdefender.nagarsewahackthon.Admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deepdefender.nagarsewahackthon.R;

import java.util.List;

public class AssignedComplaintAdapter extends RecyclerView.Adapter<AssignedComplaintAdapter.ViewHolder> {
    private List<Complaint> list;

    public AssignedComplaintAdapter(List<Complaint> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assigned_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.id.setText(item.getSubtitle());

        // Logic: Hide the divider for the last item so it looks cleaner
        if (position == list.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, id;
        View divider;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.assignTitle);
            id = itemView.findViewById(R.id.assignId);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}