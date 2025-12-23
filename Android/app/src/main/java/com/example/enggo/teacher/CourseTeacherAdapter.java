package com.example.enggo.teacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.util.List;

public class CourseTeacherAdapter extends RecyclerView.Adapter<CourseTeacherAdapter.CourseViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(CourseTeacher course);
    }

    private Context context;
    private List<CourseTeacher> courseItems;
    private OnCourseClickListener listener;

    public CourseTeacherAdapter(Context context, List<CourseTeacher> courseItems, OnCourseClickListener listener) {
        this.context = context;
        this.courseItems = courseItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.course_teacher_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseTeacher course = courseItems.get(position);
        holder.tvCourseName.setText(course.getName());
        holder.tvClassCode.setText(course.getClassCode());
        holder.tvLessons.setText(course.getLessonCount() + " lesson");

        // Click on whole card to navigate to course detail
        holder.itemView.setOnClickListener(v -> listener.onCourseClick(course));
    }

    @Override
    public int getItemCount() {
        return courseItems.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvClassCode, tvLessons;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName_teacheritem);
            tvClassCode = itemView.findViewById(R.id.tvClassCode_teacheritem);
            tvLessons = itemView.findViewById(R.id.tvLessons_teacheritem);
        }
    }
}
