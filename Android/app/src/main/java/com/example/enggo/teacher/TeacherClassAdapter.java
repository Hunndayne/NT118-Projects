package com.example.enggo.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.util.List;

public class TeacherClassAdapter extends RecyclerView.Adapter<TeacherClassAdapter.ViewHolder> {

    public interface OnClassClickListener {
        void onClassClick(ClassResponse classResponse);
    }

    private final List<ClassResponse> items;
    private final OnClassClickListener listener;

    public TeacherClassAdapter(List<ClassResponse> items, OnClassClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_teacher_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassResponse item = items.get(position);
        String name = item.name == null ? "Class" : item.name;
        holder.tvName.setText(name);
        holder.tvCode.setText("Class ID: " + (item.id == null ? "-" : item.id));
        holder.tvLessons.setText("Course ID: " + (item.courseId == null ? "-" : item.courseId));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClassClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCode;
        final TextView tvLessons;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCourseName_teacheritem);
            tvCode = itemView.findViewById(R.id.tvClassCode_teacheritem);
            tvLessons = itemView.findViewById(R.id.tvLessons_teacheritem);
        }
    }
}
