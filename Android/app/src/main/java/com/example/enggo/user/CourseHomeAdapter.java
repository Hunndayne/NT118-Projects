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

public class CourseHomeAdapter extends RecyclerView.Adapter<CourseHomeAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(CourseAdmin course);
    }

    private final List<CourseAdmin> courses;
    private final OnCourseClickListener listener;

    public CourseHomeAdapter(List<CourseAdmin> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_home_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseAdmin course = courses.get(position);
        String title = course.getName();
        if (course.getClassCode() != null && !course.getClassCode().trim().isEmpty()) {
            title = title + " " + course.getClassCode();
        }
        holder.tvTitle.setText(title);
        holder.itemView.setOnClickListener(v -> listener.onCourseClick(course));
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
        }
    }
}
