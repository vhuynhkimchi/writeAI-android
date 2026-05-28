package com.example.writeai_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.writeai_android.R;
import com.example.writeai_android.ui.main.MainActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;
    private ImageView imgTogglePassword, imgToggleConfirmPassword;
    private LinearLayout btnGoogleRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initViews();
        handleEvents();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);

        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
    }

    private void handleEvents() {
        btnRegister.setOnClickListener(v -> register());

        tvGoLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        imgToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        btnGoogleRegister.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("open_google_login", true);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Họ tên không được rỗng");
            etFullName.requestFocus();
            return;
        }

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

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng nhập lại mật khẩu");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu nhập lại không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    if (firebaseUser == null) {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Đăng ký");

                        Toast.makeText(
                                RegisterActivity.this,
                                "Không lấy được thông tin người dùng.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    saveUserToFirestore(firebaseUser, fullName, email);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Đăng ký");

                    String message = "Đăng ký thất bại";

                    if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Toast.makeText(
                            RegisterActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, String fullName, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("createdAt", Timestamp.now());
        userData.put("streakCount", 0);
        userData.put("totalEssay", 0);
        userData.put("averageScore", 0.0);
        userData.put("lastPracticeDate", "");

        firestore.collection("users")
                .document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Đăng ký thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    goToMain();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Đăng ký");

                    String message = "Tạo hồ sơ người dùng thất bại";

                    if (e.getMessage() != null) {
                        message = e.getMessage();
                    }

                    Toast.makeText(
                            RegisterActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
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

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isConfirmPasswordVisible = false;
        } else {
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isConfirmPasswordVisible = true;
        }

        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }

    private void goToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}