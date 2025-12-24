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

public class AssignmentSubmissionsAdapter extends RecyclerView.Adapter<AssignmentSubmissionsAdapter.ViewHolder> {

    public interface OnAssignmentClickListener {
        void onClick(AssignmentResponse assignment);
    }

    private final List<AssignmentResponse> assignments;
    private final OnAssignmentClickListener listener;
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat[] inputFormats = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    };

    public AssignmentSubmissionsAdapter(List<AssignmentResponse> assignments, OnAssignmentClickListener listener) {
        this.assignments = assignments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.assignment_submission_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentResponse assignment = assignments.get(position);
        holder.tvTitle.setText(assignment.title == null ? "-" : assignment.title);
        holder.tvDesc.setText(assignment.description == null || assignment.description.trim().isEmpty()
                ? "No description"
                : assignment.description);
        holder.tvDue.setText("Due: " + formatDate(assignment.deadline));
        holder.itemView.setOnClickListener(v -> listener.onClick(assignment));
    }

    @Override
    public int getItemCount() {
        return assignments.size();
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
        final TextView tvTitle;
        final TextView tvDue;
        final TextView tvDesc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAssignmentTitle);
            tvDue = itemView.findViewById(R.id.tvAssignmentDue);
            tvDesc = itemView.findViewById(R.id.tvAssignmentDesc);
        }
    }
}
