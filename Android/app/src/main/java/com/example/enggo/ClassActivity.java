package com.example.enggo;
import android.os.Bundle;

public class ClassActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_course);
        setupHeader();
        setupFooter();
    }
}
