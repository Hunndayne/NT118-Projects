package com.example.enggo.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;
import com.example.enggo.admin.CourseParticipant;

import java.util.List;

public class StudentListTeacherAdapter extends RecyclerView.Adapter<StudentListTeacherAdapter.ViewHolder> {

    private final List<CourseParticipant> students;

    public StudentListTeacherAdapter(List<CourseParticipant> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_list_item_teacher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseParticipant student = students.get(position);
        holder.tvName.setText(student.getDisplayName());
        String idText = student.getId() != null ? String.valueOf(student.getId()) : "-";
        holder.tvId.setText("ID: " + idText);
        if (student.isActive()) {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setBackgroundColor(0xFFE8F5E9);
            holder.tvStatus.setTextColor(0xFF2E7D32);
        } else {
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setBackgroundColor(0xFFFFEBEE);
            holder.tvStatus.setTextColor(0xFFC62828);
        }
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvId;
        final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvId = itemView.findViewById(R.id.tvStudentId);
            tvStatus = itemView.findViewById(R.id.tvStudentStatus);
        }
    }
}
