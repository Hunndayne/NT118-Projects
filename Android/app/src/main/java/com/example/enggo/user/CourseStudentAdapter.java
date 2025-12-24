package com.example.enggo.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;
import com.example.enggo.admin.CourseAdmin;

import java.util.List;

public class CourseStudentAdapter extends RecyclerView.Adapter<CourseStudentAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(CourseAdmin course);
    }

    private final List<CourseAdmin> courses;
    private final OnCourseClickListener listener;

    public CourseStudentAdapter(List<CourseAdmin> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_student_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseAdmin course = courses.get(position);
        holder.tvName.setText(course.getName());
        holder.tvCode.setText(course.getClassCode());
        holder.tvTag.setText("Course");
        holder.itemView.setOnClickListener(v -> listener.onCourseClick(course));
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCode;
        final TextView tvTag;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCourseName);
            tvCode = itemView.findViewById(R.id.tvCourseCode);
            tvTag = itemView.findViewById(R.id.tvCourseTag);
        }
    }
}
