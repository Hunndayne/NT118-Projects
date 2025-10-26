package com.example.enggo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Tạm thời vẫn dùng List<String>
public class CourseAdminAdapter extends RecyclerView.Adapter<CourseAdminAdapter.CourseViewHolder> {

    private List<String> courseNameList;
    private Context context;

    public CourseAdminAdapter(Context context, List<String> courseNameList) {
        this.context = context;
        this.courseNameList = courseNameList;
    }

    // Class ViewHolder: Nắm giữ các view trong "course_management_list_item.xml"
    public class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourseName;
        TextView btnEdit;
        TextView btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ánh xạ các view BÊN TRONG item
            tvCourseName = itemView.findViewById(R.id.tvCourseName_manageusritem);
            btnEdit = itemView.findViewById(R.id.btnEdit_managecourseitem);
            btnDelete = itemView.findViewById(R.id.btnDelete_managecourseitem);

            // === GẮN 2 SỰ KIỆN CLICK MÀ CỌU MUỐN ===

            // 1. Click vào NÚT EDIT -> Mở trang SỬA KHÓA HỌC
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                String courseName = courseNameList.get(position);

                Intent intent = new Intent(context, CreateCourseAdminActivity.class);
                // intent.putExtra("COURSE_NAME", courseName); // Gửi tên qua
                context.startActivity(intent);
            });

            // 2. Click vào TOÀN BỘ ITEM -> Mở trang DANH SÁCH BÀI HỌC
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                String courseName = courseNameList.get(position);

                Intent intent = new Intent(context, ManageLessonsAdminActivity.class);
                // intent.putExtra("COURSE_NAME", courseName); // Gửi tên qua
                context.startActivity(intent);
            });

        }
    }


    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout "course_management_list_item.xml"
        View view = LayoutInflater.from(context).inflate(R.layout.course_management_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        String courseName = courseNameList.get(position);
        holder.tvCourseName.setText(courseName);
    }

    @Override
    public int getItemCount() {
        return courseNameList.size();
    }
}