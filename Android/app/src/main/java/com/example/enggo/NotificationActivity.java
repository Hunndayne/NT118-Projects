package com.example.enggo;

import android.os.Bundle;

public class NotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noti_chua_xong);
        setupHeader();
        setupFooter();
    }
}
