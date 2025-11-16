package com.example.enggo.user;
import com.example.enggo.R;

import android.os.Bundle;

public class NotificationUserActivity extends BaseUserActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        setupHeader();
        setupFooter();
    }
}