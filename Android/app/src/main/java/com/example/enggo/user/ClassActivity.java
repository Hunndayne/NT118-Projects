package com.example.enggo.user;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Thêm import này
import android.widget.LinearLayout; // Thêm import này
import android.widget.ImageView; // Thêm import này
import androidx.cardview.widget.CardView; // Thêm import này

public class ClassActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tên layout của bạn là "class_course", khớp với file XML bạn đã gửi
        setContentView(R.layout.class_course);
        setupHeader();
        setupFooter();

        // === BẮT ĐẦU CODE THÊM MỚI ===

        // === ÁNH XẠ CÁC VIEW HEADER (ĐỂ CLICK) ===
        LinearLayout headerAnnouncement = findViewById(R.id.headerAnnouncement);
        LinearLayout headerLecture = findViewById(R.id.headerLecture);
        LinearLayout headerHomework = findViewById(R.id.headerHomework);
        LinearLayout headerForum = findViewById(R.id.headerForum);

        // === ÁNH XẠ CÁC VIEW CONTENT (ĐỂ ẨN/HIỆN) ===
        // Đảm bảo bạn đã thêm các ID này vào file XML class_course.xml
        CardView contentAnnouncement = findViewById(R.id.contentAnnouncement);
        CardView contentLecture = findViewById(R.id.contentLecture);
        CardView contentHomework = findViewById(R.id.contentHomework);
        CardView contentForum = findViewById(R.id.contentForum);

        // === ÁNH XẠ CÁC MŨI TÊN (ĐỂ XOAY) ===
        ImageView imgArrow1 = findViewById(R.id.imgArrow1);
        ImageView imgArrow2 = findViewById(R.id.imgArrow2);
        ImageView imgArrowHomework = findViewById(R.id.imgArrowHomework);
        ImageView imgArrow4 = findViewById(R.id.imgArrow4);

        // === THIẾT LẬP SỰ KIỆN CLICK ===
        headerAnnouncement.setOnClickListener(v -> {
            toggleSection(contentAnnouncement, imgArrow1);
        });

        headerLecture.setOnClickListener(v -> {
            toggleSection(contentLecture, imgArrow2);
        });

        headerHomework.setOnClickListener(v -> {
            toggleSection(contentHomework, imgArrowHomework);
        });

        headerForum.setOnClickListener(v -> {
            toggleSection(contentForum, imgArrow4);
        });

        LinearLayout homework = findViewById(R.id.hw1);
        homework.setOnClickListener(v -> {
            Intent intent = new Intent(ClassActivity.this, SubmitHomeworkActivity.class);
            startActivity(intent);
        });


        // === KẾT THÚC CODE THÊM MỚI ===
    }

    /**
     * Hàm trợ giúp để ản/hiện nội dung và xoay mũi tên
     * @param content View nội dung (CardView) để ản/hiện
     * @param arrow View mũi tên (ImageView) để xoay
     */
    private void toggleSection(View content, View arrow) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;

        if (isVisible) {
            // Nếu đang hiện -> ẩn đi
            content.setVisibility(View.GONE);
            // Xoay mũi tên về 0 độ (chỉ xuống)
            arrow.animate().rotation(0).setDuration(300).start();
        } else {
            // Nếu đang ẩn -> hiện ra
            content.setVisibility(View.VISIBLE);
            // Xoay mũi tên 180 độ (chỉ lên)
            arrow.animate().rotation(180).setDuration(300).start();
        }
    }

}