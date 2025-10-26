package com.example.enggo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LessonAdminAdapter extends RecyclerView.Adapter<LessonAdminAdapter.LessonViewHolder> {

    private List<String> lessonNameList;
    private Context context;

    // Hàm khởi tạo để nhận dữ liệu
    public LessonAdminAdapter(Context context, List<String> lessonNameList) {
        this.context = context;
        this.lessonNameList = lessonNameList;
    }
    public class LessonViewHolder extends RecyclerView.ViewHolder {

        // Khai báo các view bên trong "lesson_management_list_item.xml"
        TextView tvLessonName;
        TextView btnEdit;
        TextView btnDelete;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLessonName = itemView.findViewById(R.id.tvLessonName_manageusritem);
            btnEdit = itemView.findViewById(R.id.btnEdit_manageLessonitem);

            // === GẮN SỰ KIỆN CLICK CHO NÚT EDIT ===
            btnEdit.setOnClickListener(v -> {
                // Lấy vị trí của item được click
                int position = getAdapterPosition();

                // Lấy tên bài học (hoặc ID) từ danh sách
                String lessonName = lessonNameList.get(position);

                // Tạo Intent để mở EditLessonAdminActivity
                Intent intent = new Intent(context, EditLessonAdminActivity.class);

                context.startActivity(intent);
            });
            itemView.setOnClickListener(v -> {
               Intent intent = new Intent(context, AssignmentsManagementAdminActivity.class);
               context.startActivity(intent);

            });
        }
    }

    // ========================================================
    // CÁC HÀM BẮT BUỘC CỦA ADAPTER
    // ========================================================

    // 1. Hàm này tạo ra cái ViewHolder (ở trên)
    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout "lesson_management_list_item.xml" của cọu
        View view = LayoutInflater.from(context).inflate(R.layout.lesson_management_list_item, parent, false);
        return new LessonViewHolder(view);
    }

    // 2. Hàm này "nhét" dữ liệu vào item
    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        // Lấy tên bài học ở vị trí "position"
        String lessonName = lessonNameList.get(position);

        // Gắn tên bài học lên TextView
        holder.tvLessonName.setText(lessonName);

    }

    // 3. Hàm này báo cho RecyclerView biết có bao nhiêu item
    @Override
    public int getItemCount() {
        return lessonNameList.size();
    }
}