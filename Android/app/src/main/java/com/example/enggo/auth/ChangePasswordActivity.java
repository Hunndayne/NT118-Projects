package com.example.enggo.auth;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.api.ForgotPasswordRequest;
import com.example.enggo.api.MessageResponse;
import com.example.enggo.api.ResetPasswordRequest;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

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

        btnSendEmail.setOnClickListener(v -> sendOtp());
        btnResetPassword.setOnClickListener(v -> resetPassword());

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

    private void sendOtp() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email không được trống", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "OTP đã được gửi về email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Gửi OTP thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this,
                        "Không kết nối được server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String otp = getOtp();
        String newPassword = etNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email không được trống", Toast.LENGTH_SHORT).show();
            return;
        }
        if (otp.length() != 4) {
            Toast.makeText(this, "OTP phải đủ 4 số", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Mật khẩu mới không được trống", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.resetPassword(new ResetPasswordRequest(email, otp, newPassword))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "OTP không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        Toast.makeText(ChangePasswordActivity.this,
                                "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getOtp() {
        return etOtp1.getText().toString().trim()
                + etOtp2.getText().toString().trim()
                + etOtp3.getText().toString().trim()
                + etOtp4.getText().toString().trim();
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
