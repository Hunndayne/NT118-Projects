package com.example.enggo.user;
import com.example.enggo.R;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgetPasswordActivity extends AppCompatActivity {

    EditText etEmail, etOtp1, etOtp2, etOtp3, etOtp4, etNewPassword;
    Button btnSendEmail, btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.forget_password);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);

        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);

        btnSendEmail = findViewById(R.id.btnSendEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                Toast.makeText(ForgetPasswordActivity.this, "Đang gửi OTP tới: " + email, Toast.LENGTH_SHORT).show();
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = etOtp1.getText().toString() +
                        etOtp2.getText().toString() +
                        etOtp3.getText().toString() +
                        etOtp4.getText().toString();

                String newPassword = etNewPassword.getText().toString();

                Toast.makeText(ForgetPasswordActivity.this, "Đang đổi mật khẩu...", Toast.LENGTH_SHORT).show();
            }
        });

        setupOtpInputs();
    }

    private void setupOtpInputs() {
        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, null));

        etOtp2.setOnKeyListener(new OtpKeyListener(etOtp2, etOtp1));
        etOtp3.setOnKeyListener(new OtpKeyListener(etOtp3, etOtp2));
        etOtp4.setOnKeyListener(new OtpKeyListener(etOtp4, etOtp3));
    }

    private class OtpTextWatcher implements TextWatcher {
        private View currentView;
        private View nextView;

        public OtpTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }

    private class OtpKeyListener implements View.OnKeyListener {
        private EditText currentEditText;
        private EditText previousEditText;

        public OtpKeyListener(EditText currentEditText, EditText previousEditText) {
            this.currentEditText = currentEditText;
            this.previousEditText = previousEditText;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (currentEditText.getText().toString().isEmpty() && previousEditText != null) {
                    previousEditText.requestFocus();
                }
            }
            return false;
        }
    }
}