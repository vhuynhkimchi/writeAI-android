package com.example.writeai_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageView imgToggleCurrentPassword, imgToggleNewPassword, imgToggleConfirmPassword;
    private Button btnChangePassword;
    private TextView tvForgotPassword;

    private FirebaseAuth auth;

    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth = FirebaseAuth.getInstance();

        initViews();
        handleEvents();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        imgToggleCurrentPassword = findViewById(R.id.imgToggleCurrentPassword);
        imgToggleNewPassword = findViewById(R.id.imgToggleNewPassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnBack.setOnClickListener(v -> finish());
    }

    private void handleEvents() {
        btnChangePassword.setOnClickListener(v -> changePassword());

        imgToggleCurrentPassword.setOnClickListener(v -> {
            isCurrentVisible = !isCurrentVisible;
            togglePassword(etCurrentPassword, isCurrentVisible);
        });

        imgToggleNewPassword.setOnClickListener(v -> {
            isNewVisible = !isNewVisible;
            togglePassword(etNewPassword, isNewVisible);
        });

        imgToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmVisible = !isConfirmVisible;
            togglePassword(etConfirmPassword, isConfirmVisible);
        });

        tvForgotPassword.setOnClickListener(v -> sendResetPasswordToCurrentUserEmail());
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu mới tối thiểu 6 ký tự");
            etNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng nhập lại mật khẩu mới");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu nhập lại không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            etNewPassword.setError("Mật khẩu mới không được trùng mật khẩu hiện tại");
            etNewPassword.requestFocus();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
            return;
        }

        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Đang xử lý...");

        AuthCredential credential = EmailAuthProvider.getCredential(
                user.getEmail(),
                currentPassword
        );

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(result -> {
                                btnChangePassword.setEnabled(true);
                                btnChangePassword.setText("Đổi mật khẩu");

                                Toast.makeText(
                                        ChangePasswordActivity.this,
                                        "Đổi mật khẩu thành công",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnChangePassword.setEnabled(true);
                                btnChangePassword.setText("Đổi mật khẩu");

                                Toast.makeText(
                                        ChangePasswordActivity.this,
                                        "Không thể đổi mật khẩu: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Đổi mật khẩu");

                    Toast.makeText(
                            ChangePasswordActivity.this,
                            "Mật khẩu hiện tại không đúng",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void sendResetPasswordToCurrentUserEmail() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(
                    ChangePasswordActivity.this,
                    "Bạn chưa đăng nhập.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String email = user.getEmail();

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(
                    ChangePasswordActivity.this,
                    "Tài khoản này không có email để gửi link đổi mật khẩu.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        tvForgotPassword.setEnabled(false);
        tvForgotPassword.setText("Đang gửi link đổi mật khẩu...");

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    tvForgotPassword.setEnabled(true);
                    tvForgotPassword.setText("Bạn quên mật khẩu? Nhấn vào link để đổi mật khẩu");

                    Toast.makeText(
                            ChangePasswordActivity.this,
                            "Đã gửi link đổi mật khẩu đến email: " + email,
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {
                    tvForgotPassword.setEnabled(true);
                    tvForgotPassword.setText("Bạn quên mật khẩu? Nhấn vào link để đổi mật khẩu");

                    String message = "Không thể gửi email đổi mật khẩu.";

                    if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Toast.makeText(
                            ChangePasswordActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void togglePassword(EditText editText, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        editText.setSelection(editText.getText().length());
    }
}