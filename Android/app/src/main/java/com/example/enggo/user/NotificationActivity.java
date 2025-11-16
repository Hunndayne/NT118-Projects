package com.example.enggo.user;
import com.example.enggo.R;

import android.os.Bundle;

public class NotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        setupHeader();
        setupFooter();
    }
}