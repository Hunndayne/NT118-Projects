package com.example.enggo.admin;
import com.example.enggo.R;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdminAdapter extends RecyclerView.Adapter<CourseAdminAdapter.CourseViewHolder> {

    // Thay đổi danh sách dữ liệu để sử dụng CourseItem
    private List<ManageCoursesAdminActivity.CourseItem> courseItems;
    private Context context;

    // Cập nhật hàm khởi tạo để chấp nhận List<CourseItem>
    public CourseAdminAdapter(Context context, List<ManageCoursesAdminActivity.CourseItem> courseItems) {
        this.context = context;
        this.courseItems = courseItems;
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourseName;
        TextView tvClassCode;
        TextView tvLessonCount;
        TextView btnEdit;
        TextView btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ánh xạ các view BÊN TRONG item
            tvCourseName = itemView.findViewById(R.id.tvCourseName_manageusritem);
            tvClassCode = itemView.findViewById(R.id.tvClassCode_manageusritem);
            tvLessonCount = itemView.findViewById(R.id.tvLessons_manageusritem);
            btnEdit = itemView.findViewById(R.id.btnEdit_managecourseitem);
            btnDelete = itemView.findViewById(R.id.btnDelete_managecourseitem);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ManageCoursesAdminActivity.CourseItem courseItem = courseItems.get(position);
                    Intent intent = new Intent(context, EditCourseAdminActivity.class);
                    // Gửi dữ liệu qua intent nếu cần
                    // intent.putExtra("COURSE_NAME", courseItem.name);
                    context.startActivity(intent);
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ManageCoursesAdminActivity.CourseItem courseItem = courseItems.get(position);
                    Intent intent = new Intent(context, ManageLessonsAdminActivity.class);
                    // Gửi dữ liệu qua intent nếu cần
                    // intent.putExtra("COURSE_NAME", courseItem.name);
                    context.startActivity(intent);
                }
            });
        }
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_management_list_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        ManageCoursesAdminActivity.CourseItem courseItem = courseItems.get(position);
        holder.tvCourseName.setText(courseItem.name);
        holder.tvClassCode.setText(courseItem.classCode);
        holder.tvLessonCount.setText(String.format("%d Lessons", courseItem.lessonCount));
    }

    @Override
    public int getItemCount() {
        return courseItems.size();
    }
}