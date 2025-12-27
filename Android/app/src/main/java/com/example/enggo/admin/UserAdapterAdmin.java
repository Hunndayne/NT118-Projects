package com.example.enggo.admin;

import com.example.enggo.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapterAdmin extends RecyclerView.Adapter<UserAdapterAdmin.UserViewHolder> {

    private Context context;
    private List<UserAdmin> userList;
    private OnUserActionsListener listener;

    // 1. Interface để gửi sự kiện click ra bên ngoài (về Activity)
    public interface OnUserActionsListener {
        void onEditClick(UserAdmin user);
        void onDeleteClick(UserAdmin user);
        void onLockClick(UserAdmin user);
    }

    public UserAdapterAdmin(Context context, List<UserAdmin> userList, OnUserActionsListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 2. Dùng file layout item của bạn
        View view = LayoutInflater.from(context).inflate(R.layout.user_management_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserAdmin user = userList.get(position);

        holder.tvUserName.setText(user.getFullName());
        holder.tvUserStatus.setText(user.getStatusText());

        holder.btnEdit.setOnClickListener(v -> {
            Log.e("EDIT_TEST", "EDIT CLICKED: " + user.getId());
            listener.onEditClick(user);
        });
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(user));
        holder.btnLock.setOnClickListener(v -> listener.onLockClick(user));
        holder.tvUserStatus.setText(user.isActive() ? "Active" : "Locked");
        holder.btnLock.setText(user.isActive() ? "Lock" : "Unlock");

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Class ViewHolder để giữ các View trong item layout
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserStatus;
        TextView btnEdit, btnDelete, btnLock;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName_manageusritem);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus_manageusritem);

            btnEdit = itemView.findViewById(R.id.btnEdit_manageusritem);
            btnDelete = itemView.findViewById(R.id.btnDelete_manageusritem);
            btnLock = itemView.findViewById(R.id.btnLock_manageusritem);
        }
    }
}
