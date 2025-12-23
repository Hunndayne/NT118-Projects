package com.example.enggo.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;

import java.util.List;

public class LessonTeacherAdapter extends RecyclerView.Adapter<LessonTeacherAdapter.ViewHolder> {

    public interface OnLessonActionListener {
        void onEdit(LessonResponse lesson);
        void onDelete(LessonResponse lesson);
    }

    private final List<LessonResponse> lessons;
    private final OnLessonActionListener listener;

    public LessonTeacherAdapter(List<LessonResponse> lessons, OnLessonActionListener listener) {
        this.lessons = lessons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lesson_management_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LessonResponse lesson = lessons.get(position);
        holder.tvName.setText(lesson.title == null ? "Lesson" : lesson.title);
        String orderText = lesson.orderIndex == null ? "Order: -" : "Order: " + lesson.orderIndex;
        holder.tvDate.setText(orderText);
        holder.tvPoster.setText("Teacher");
        holder.tvDescription.setText(lesson.description == null ? "" : lesson.description);
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(lesson);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDelete(lesson);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvDate;
        final TextView tvPoster;
        final TextView tvDescription;
        final TextView btnEdit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLessonName_manageusritem);
            tvDate = itemView.findViewById(R.id.tvLessonDate_manageusritem);
            tvPoster = itemView.findViewById(R.id.tvLessonPoster_manageusritem);
            tvDescription = itemView.findViewById(R.id.tvLessonDescription_manageusritem);
            btnEdit = itemView.findViewById(R.id.btnEdit_manageLessonitem);
        }
    }
}
