package com.example.enggo;

import android.os.Bundle;

public class ManageAccountAdminActivity extends BaseAdminActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_account_admin);

        setupAdminHeader();
        setupAdminFooter();

    }
}
