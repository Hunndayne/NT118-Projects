package com.example.enggo.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.util.List;

public class CourseAdminAdapter
        extends RecyclerView.Adapter<CourseAdminAdapter.CourseViewHolder> {

    private Context context;
    private List<CourseAdmin> courseItems;

    public CourseAdminAdapter(Context context, List<CourseAdmin> courseItems) {
        this.context = context;
        this.courseItems = courseItems;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.course_management_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseAdmin course = courseItems.get(position);

        holder.tvCourseName.setText(course.getName());
        holder.tvClassCode.setText(course.getClassCode());
        holder.tvLessons.setText(course.getLessonCount() + " lesson");
    }

    @Override
    public int getItemCount() {
        return courseItems.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourseName, tvClassCode, tvLessons;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCourseName = itemView.findViewById(R.id.tvCourseName_manageusritem);
            tvClassCode = itemView.findViewById(R.id.tvClassCode_manageusritem);
            tvLessons = itemView.findViewById(R.id.tvLessons_manageusritem);
        }
    }
}
