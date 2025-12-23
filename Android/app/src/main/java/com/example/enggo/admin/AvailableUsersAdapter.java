package com.example.enggo.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.util.List;

public class AvailableUsersAdapter extends RecyclerView.Adapter<AvailableUsersAdapter.ViewHolder> {

    public interface OnAvailableUserActionListener {
        void onAdd(CourseParticipant participant);
    }

    private final List<CourseParticipant> items;
    private final OnAvailableUserActionListener listener;

    public AvailableUsersAdapter(List<CourseParticipant> items, OnAvailableUserActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_available_to_add_course_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseParticipant participant = items.get(position);
        holder.tvName.setText(participant.getDisplayName());
        holder.tvEmail.setText("ID: " + participant.getId());
        holder.tvStatus.setText("Available");
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdd(participant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvEmail;
        final TextView tvStatus;
        final TextView btnAdd;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName_manageusritem);
            tvEmail = itemView.findViewById(R.id.tvUserEmail_manageusritem);
            tvStatus = itemView.findViewById(R.id.tvUserStatus_manageusritem);
            btnAdd = itemView.findViewById(R.id.btnAdd_participant);
        }
    }
}
