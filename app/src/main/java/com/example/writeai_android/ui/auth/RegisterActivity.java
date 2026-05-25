package com.example.writeai_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> register());
        tvGoLogin.setOnClickListener(v -> finish());
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
            etPassword.setError("Password không được rỗng");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password tối thiểu 6 ký tự");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Confirm Password phải trùng Password");
            etConfirmPassword.requestFocus();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        Toast.makeText(this, "Không lấy được thông tin user.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", firebaseUser.getUid());
                    userData.put("fullName", fullName);
                    userData.put("email", email);
                    userData.put("createdAt", Timestamp.now());
                    userData.put("streakCount", 0);
                    userData.put("totalEssay", 0);
                    userData.put("averageScore", 0.0);
                    userData.put("lastPracticeDate", "");

                    firestore.collection("users").document(firebaseUser.getUid())
                            .set(userData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                goToMain();
                            })
                            .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this,
                                    "Tạo hồ sơ người dùng thất bại: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this,
                        "Đăng ký thất bại: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void goToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
