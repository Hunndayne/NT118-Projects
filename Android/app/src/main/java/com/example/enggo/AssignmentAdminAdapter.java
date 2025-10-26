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

// Tạm thời dùng List<String>
public class AssignmentAdminAdapter extends RecyclerView.Adapter<AssignmentAdminAdapter.AssignmentViewHolder> {

    private List<String> assignmentNameList;
    private Context context;

    public AssignmentAdminAdapter(Context context, List<String> assignmentNameList) {
        this.context = context;
        this.assignmentNameList = assignmentNameList;
    }

    public class AssignmentViewHolder extends RecyclerView.ViewHolder {

        // Views bên trong assignment_management_list_item.xml
        // (Tui dùng ID từ file manage_assignment_item.xml cọu gửi lúc trước)
        TextView tvAssignmentName;
        TextView btnEdit;
        TextView btnDelete;
        TextView btnStatistic;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvAssignmentName = itemView.findViewById(R.id.tvAssignmentName_manageassitem);
            btnEdit = itemView.findViewById(R.id.btnEdit_manageAssignmentitem);
            btnDelete = itemView.findViewById(R.id.btnDelete_manageAssignmentitem);
            btnStatistic = itemView.findViewById(R.id.btnStatistic_manageAssignmentitem);

            // Click nút Edit -> Mở EditAssignmentAdminActivity
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                String assignmentName = assignmentNameList.get(position);
                Intent intent = new Intent(context, EditAssignmentAdminActivity.class);
                context.startActivity(intent);
            });
        }
    }

    // --- Các hàm bắt buộc ---
    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout item của cọu
        View view = LayoutInflater.from(context).inflate(R.layout.assignment_management_list_item, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        String assignmentName = assignmentNameList.get(position);
        holder.tvAssignmentName.setText(assignmentName);
    }

    @Override
    public int getItemCount() {
        return assignmentNameList.size();
    }
}