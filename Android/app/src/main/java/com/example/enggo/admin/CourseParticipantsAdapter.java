package com.example.enggo.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.util.List;

public class CourseParticipantsAdapter extends RecyclerView.Adapter<CourseParticipantsAdapter.ViewHolder> {

    public interface OnParticipantActionListener {
        void onRemove(CourseParticipant participant);
    }

    private final List<CourseParticipant> items;
    private final OnParticipantActionListener listener;

    public CourseParticipantsAdapter(List<CourseParticipant> items, OnParticipantActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_participant_management_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseParticipant participant = items.get(position);
        holder.tvName.setText(participant.getDisplayName());
        holder.tvEmail.setText("ID: " + participant.getId());
        holder.tvStatus.setText("Participant");
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(participant);
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
        final TextView btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName_manageusritem);
            tvEmail = itemView.findViewById(R.id.tvUserEmail_manageusritem);
            tvStatus = itemView.findViewById(R.id.tvUserStatus_manageusritem);
            btnRemove = itemView.findViewById(R.id.btnRemove_participant);
        }
    }
}
