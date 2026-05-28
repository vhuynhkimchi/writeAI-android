package com.example.writeai_android.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendReset;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);

        btnBack.setOnClickListener(v -> finish());

        btnSendReset.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        btnSendReset.setEnabled(false);
        btnSendReset.setText("Đang gửi...");

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    btnSendReset.setEnabled(true);
                    btnSendReset.setText("Gửi link đặt lại mật khẩu");

                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            "Nếu email này đã được đăng ký, hệ thống sẽ gửi link đặt lại mật khẩu. Vui lòng kiểm tra hộp thư.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSendReset.setEnabled(true);
                    btnSendReset.setText("Gửi link đặt lại mật khẩu");

                    String message = "Không thể gửi email đặt lại mật khẩu";

                    if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}