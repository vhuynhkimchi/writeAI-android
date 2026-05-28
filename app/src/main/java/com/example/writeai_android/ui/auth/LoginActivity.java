package com.example.writeai_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.example.writeai_android.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_TEST";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister, tvForgotPassword;
    private ImageView imgTogglePassword;
    private LinearLayout btnGoogleLogin;

    private FirebaseAuth auth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        initViews();
        handleEvents();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
    }

    private void handleEvents() {
        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Đã bấm nút đăng nhập");
            Toast.makeText(this, "Đang xử lý đăng nhập...", Toast.LENGTH_SHORT).show();
            login();
        });

        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        btnGoogleLogin.setOnClickListener(v -> {
            Toast.makeText(
                    LoginActivity.this,
                    "Chức năng đăng nhập Google sẽ xử lý ở đây",
                    Toast.LENGTH_SHORT
            ).show();

            // Sau này nếu bạn đã có hàm Google Sign-In thì đổi thành:
            // signInWithGoogle();
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Email nhập: " + email);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email không được rỗng");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu không được rỗng");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(
                            LoginActivity.this,
                            "Đăng nhập thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    goToMain();
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng Nhập");

                    String message = "Đăng nhập thất bại.";

                    if (e instanceof FirebaseAuthInvalidUserException) {
                        message = "Email chưa được đăng ký hoặc tài khoản không tồn tại.";
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Email hoặc mật khẩu không đúng.";
                    } else if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Log.e(TAG, "Lỗi đăng nhập: ", e);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Nhập email để khôi phục mật khẩu");
            etEmail.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            LoginActivity.this,
                            "Đã gửi email khôi phục mật khẩu",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {
                    String message = "Không thể gửi email khôi phục mật khẩu";

                    if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPasswordVisible = false;
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isPasswordVisible = true;
        }

        etPassword.setSelection(etPassword.getText().length());
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}