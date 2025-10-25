package com.example.enggo;

import android.os.Bundle;

public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_chua_xong);
        setupHeader();
        setupFooter();
    }
}
