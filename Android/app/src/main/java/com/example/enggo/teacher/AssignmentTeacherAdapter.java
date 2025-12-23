package com.example.enggo.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentTeacherAdapter extends RecyclerView.Adapter<AssignmentTeacherAdapter.ViewHolder> {

    public interface OnAssignmentActionListener {
        void onEdit(AssignmentResponse assignment);
        void onDelete(AssignmentResponse assignment);
    }

    private final List<AssignmentResponse> assignments;
    private final OnAssignmentActionListener listener;
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat[] inputFormats = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    };

    public AssignmentTeacherAdapter(List<AssignmentResponse> assignments, OnAssignmentActionListener listener) {
        this.assignments = assignments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.assignment_management_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentResponse assignment = assignments.get(position);
        holder.tvName.setText(safeText(assignment.title));
        holder.tvDescription.setText(safeText(assignment.description));
        holder.tvFromDate.setText("From: " + formatDate(assignment.createdAt));
        holder.tvDueDate.setText("Due: " + formatDate(assignment.deadline));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(assignment));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(assignment));
        holder.btnStatistic.setOnClickListener(v -> {
            // placeholder
        });
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        for (SimpleDateFormat input : inputFormats) {
            try {
                Date parsed = input.parse(value);
                if (parsed != null) {
                    return outputFormat.format(parsed);
                }
            } catch (ParseException ignored) {
                // try next
            }
        }
        return value;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvFromDate;
        final TextView tvDueDate;
        final TextView tvDescription;
        final TextView btnEdit;
        final TextView btnDelete;
        final TextView btnStatistic;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAssignmentName_manageassitem);
            tvFromDate = itemView.findViewById(R.id.tvFromDate_manageassitem);
            tvDueDate = itemView.findViewById(R.id.tvDueDate_manageassitem);
            tvDescription = itemView.findViewById(R.id.tvDescriptionAssignment_manageassitem);
            btnEdit = itemView.findViewById(R.id.btnEdit_manageAssignmentitem);
            btnDelete = itemView.findViewById(R.id.btnDelete_manageAssignmentitem);
            btnStatistic = itemView.findViewById(R.id.btnStatistic_manageAssignmentitem);
        }
    }
}
