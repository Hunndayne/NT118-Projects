package com.example.enggo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class EditInformationActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_information); // file profile.xml trong res/layout

        // Xử lý nút Back
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());
        setupHeader();
        setupFooter();
    }
}
