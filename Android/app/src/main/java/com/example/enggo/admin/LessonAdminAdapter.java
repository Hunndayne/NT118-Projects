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

public class LessonAdminAdapter extends RecyclerView.Adapter<LessonAdminAdapter.LessonViewHolder> {

    private List<ManageLessonsAdminActivity.LessonItem> lessonItems;
    private Context context;

    public LessonAdminAdapter(Context context, List<ManageLessonsAdminActivity.LessonItem> lessonItems) {
        this.context = context;
        this.lessonItems = lessonItems;
    }

    public class LessonViewHolder extends RecyclerView.ViewHolder {

        TextView tvLessonName;
        TextView tvLessonDate;
        TextView tvLessonPoster;
        TextView tvLessonDescription;
        TextView btnEdit;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLessonName = itemView.findViewById(R.id.tvLessonName_manageusritem);
            tvLessonDate = itemView.findViewById(R.id.tvLessonDate_manageusritem);
            tvLessonPoster = itemView.findViewById(R.id.tvLessonPoster_manageusritem);
            tvLessonDescription = itemView.findViewById(R.id.tvLessonDescription_manageusritem);
            btnEdit = itemView.findViewById(R.id.btnEdit_manageLessonitem);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ManageLessonsAdminActivity.LessonItem lessonItem = lessonItems.get(position);
                    Intent intent = new Intent(context, EditLessonAdminActivity.class);
                    context.startActivity(intent);
                }
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AssignmentsManagementAdminActivity.class);
                context.startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lesson_management_list_item, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        ManageLessonsAdminActivity.LessonItem lessonItem = lessonItems.get(position);
        holder.tvLessonName.setText(lessonItem.name);
        holder.tvLessonDate.setText(lessonItem.date);
        holder.tvLessonPoster.setText(lessonItem.poster); // Bỏ "Posted by:"
        holder.tvLessonDescription.setText(lessonItem.description);
    }

    @Override
    public int getItemCount() {
        return lessonItems.size();
    }
}