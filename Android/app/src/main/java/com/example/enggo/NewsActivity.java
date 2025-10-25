package com.example.enggo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.news);

        LinearLayout notificationContainer = findViewById(R.id.notification_container);

        String[][] demoData = {
                {"Thông báo học phí", "Bạn có khoản học phí kỳ 1 sắp hết hạn."},
                {"Lịch thi", "Đã có lịch thi môn An toàn thông tin."},
                {"Sự kiện", "Chào mừng ngày nhà giáo ViệtNam 20/11."}
        };

        LayoutInflater inflater = LayoutInflater.from(this);

        for (String[] itemData : demoData) {
            View itemView = inflater.inflate(R.layout.item_listview_simple, notificationContainer, false);

            TextView tvTitle = itemView.findViewById(R.id.tv_item_title);
            TextView tvDescription = itemView.findViewById(R.id.tv_item_description);

            tvTitle.setText(itemData[0]);
            tvDescription.setText(itemData[1]);

            notificationContainer.addView(itemView);
        }
    }
}